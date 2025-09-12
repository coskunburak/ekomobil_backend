package com.ekomobil.service;

import com.ekomobil.domain.entity.PasswordResetChallenge;
import com.ekomobil.repo.PasswordResetChallengeRepository;
import com.ekomobil.repo.UserRepository;
import com.ekomobil.security.JwtUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final PasswordResetChallengeRepository repo;
    private final JavaMailSender mailSender;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Optional so the service works even if you don't manage refresh tokens. */
    private final Optional<TokenService> tokenService;

    @Value("${app.reset.ttl-minutes:30}") private int resetTtlMinutes;
    @Value("${app.mail.from:no-reply@ekomobil.com}") private String fromAddress;

    private static final SecureRandom RND = new SecureRandom();

    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");
    private static final Pattern SPEC  = Pattern.compile("[^A-Za-z0-9]");

    /* ==================== 1) Issue OTP ==================== */
    public void issueOtp(@NonNull String email) {
        repo.findFirstByEmailAndConsumedFalseOrderByExpiresAtDesc(email).ifPresent(existing -> {
            existing.setConsumed(true);
            existing.setConsumedAt(Instant.now());
            repo.save(existing);
        });

        final String code = String.format("%06d", RND.nextInt(1_000_000));
        final String salt = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes(9));
        final String codeHash = sha256Hex(salt + ":" + code);

        var prc = new PasswordResetChallenge();
        prc.setEmail(email);
        prc.setSalt(salt);
        prc.setCodeHash(codeHash);
        prc.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));
        repo.save(prc);

        sendOtpEmail(email, code);
        log.info("Password reset OTP issued for emailHash={}", Integer.toHexString(email.hashCode()));
    }

    /* ==================== 2) Verify OTP -> Reset token ==================== */
    public String verifyOtpAndIssueToken(@NonNull String email, @NonNull String code) {
        var prc = repo.findFirstByEmailAndConsumedFalseOrderByExpiresAtDesc(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid code"));

        if (Instant.now().isAfter(prc.getExpiresAt())) {
            prc.setConsumed(true); repo.save(prc);
            throw new IllegalArgumentException("Expired");
        }
        if (prc.getAttempts() >= prc.getMaxAttempts()) {
            prc.setConsumed(true); repo.save(prc);
            throw new IllegalArgumentException("Too many attempts");
        }

        prc.setAttempts(prc.getAttempts() + 1);
        final boolean ok = sha256Hex(prc.getSalt() + ":" + code).equalsIgnoreCase(prc.getCodeHash());
        repo.save(prc);
        if (!ok) throw new IllegalArgumentException("Invalid code");

        prc.setConsumed(true);
        prc.setConsumedAt(Instant.now());
        repo.save(prc);

        // ⬇️ Strongly separated reset token
        String resetJwt = jwtUtil.createResetToken(email, Duration.ofMinutes(resetTtlMinutes));

        log.info("Password reset token issued for emailHash={}", Integer.toHexString(email.hashCode()));
        return resetJwt;
    }

    /* ==================== 3) Perform reset (atomic) ==================== */
    @Transactional
    public void performReset(@NonNull String token, @NonNull String newPassword) {
        // Parse + enforce reset-only semantics
        var claims = jwtUtil.parseAndValidateReset(token);
        final String email = claims.getSubject();

        var user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        validatePasswordPolicy(newPassword);
        if (user.getPassword() != null && passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        try { user.setPasswordChangedAt(Instant.now()); } catch (Exception ignored) {}
        userRepository.save(user);

        tokenService.ifPresent(svc -> {
            try { svc.revokeAllForUser(user.getId()); }
            catch (Exception ex) { log.warn("Token revoke failed for userId={}: {}", user.getId(), ex.getMessage()); }
        });

        repo.findFirstByEmailAndConsumedFalseOrderByExpiresAtDesc(email).ifPresent(c -> {
            c.setConsumed(true);
            c.setConsumedAt(Instant.now());
            repo.save(c);
        });

        log.info("Password reset completed for userId={}, emailHash={}",
                user.getId(), Integer.toHexString(email.hashCode()));
    }

    /* ==================== Backward compat helpers ==================== */
    public String requestResetAndReturnRawIfDev(@NonNull String email, boolean devEnabled) {
        if (devEnabled) {
            try { issueOtp(email); } catch (Exception ex) {
                log.warn("Dev mode: issueOtp failed: {}", ex.getMessage());
            }
            return jwtUtil.createResetToken(email, Duration.ofMinutes(resetTtlMinutes));
        }
        issueOtp(email);
        return null;
    }

    public void resetPassword(@NonNull String token, @NonNull String newPassword) {
        performReset(token, newPassword);
    }

    /* ==================== Mail & helpers ==================== */
    private void sendOtpEmail(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject("Ekomobil şifre sıfırlama kodunuz: " + code);

            String html = """
              <div style="font-family:Arial,Helvetica,sans-serif;font-size:14px;color:#222">
                <p>Merhaba,</p>
                <p>Şifre sıfırlama talebiniz için doğrulama kodunuz:</p>
                <div style="font-size:22px;letter-spacing:4px;font-weight:bold;margin:12px 0">%s</div>
                <p>Bu kod <b>10 dakika</b> boyunca geçerlidir. Lütfen uygulamada kodu girerek yeni şifrenizi belirleyin.</p>
                <p>Bu işlem size ait değilse, bu e-postayı yok sayabilirsiniz.</p>
                <hr style="border:none;border-top:1px solid #eee;margin:16px 0"/>
                <p style="font-size:12px;color:#777">Ekomobil Güvenlik • Otomatik iletidir, lütfen yanıtlamayınız.</p>
              </div>
              """.formatted(code);

            helper.setText(html, true);
            message.setText("Şifre sıfırlama kodunuz: " + code + " (10 dk geçerli)");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Reset OTP mail send failed to {}: {}", to, e.getMessage(), e);
            throw new IllegalStateException("Mail gönderimi başarısız", e);
        }
    }

    private static byte[] randomBytes(int len) {
        byte[] b = new byte[len];
        RND.nextBytes(b);
        return b;
    }

    private static String sha256Hex(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void validatePasswordPolicy(String pwd) {
        if (pwd == null || pwd.length() < 8 || pwd.length() > 128) {
            throw new IllegalArgumentException("Password length must be between 8 and 128 characters");
        }
        if (!UPPER.matcher(pwd).find()) throw new IllegalArgumentException("Password must contain an uppercase letter");
        if (!LOWER.matcher(pwd).find()) throw new IllegalArgumentException("Password must contain a lowercase letter");
        if (!DIGIT.matcher(pwd).find()) throw new IllegalArgumentException("Password must contain a digit");
        if (!SPEC.matcher(pwd).find())  throw new IllegalArgumentException("Password must contain a special character");
    }

    public interface TokenService {
        void revokeAllForUser(Long userId);
    }
}

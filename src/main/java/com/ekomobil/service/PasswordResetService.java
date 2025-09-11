package com.ekomobil.service;

import com.ekomobil.domain.entity.PasswordResetToken;
import com.ekomobil.domain.entity.User;
import com.ekomobil.repo.PasswordResetTokenRepository;
import com.ekomobil.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder encoder;
    private final MailService mail;

    @Value("${app.reset.base-url}") private String baseUrl;
    @Value("${app.reset.ttl-minutes:30}") private long ttlMinutes;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Transactional
    public void requestReset(String email) {
        String normalized = email == null ? "" : email.trim();
        userRepo.findByEmailIgnoreCase(normalized).ifPresent(this::createAndSendTokenFor);
    }

    @Transactional
    public String requestResetAndReturnRawIfDev(String email, boolean devEnabled) {
        if (!devEnabled) {
            requestReset(email);
            return null;
        }
        String normalized = email == null ? "" : email.trim();
        return userRepo.findByEmailIgnoreCase(normalized)
                .map(this::createAndSendTokenAndReturnRaw)
                .orElse(null);
    }

    private void createAndSendTokenFor(User user) {
        createAndSendTokenAndReturnRaw(user);
    }

    private String createAndSendTokenAndReturnRaw(User user) {
        byte[] seed = new byte[32];
        SECURE_RANDOM.nextBytes(seed);
        String raw = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(seed);
        String hash = sha256(raw);

        var entity = new PasswordResetToken();
        entity.setUser(user);
        entity.setTokenHash(hash);
        entity.setExpiresAt(Instant.now().plus(Duration.ofMinutes(ttlMinutes)));
        tokenRepo.save(entity);

        log.info("Password reset token created for userId={}, email='{}', expiresAt={}, RAW_TOKEN={}",
                user.getId(), user.getEmail(), entity.getExpiresAt(), raw);

        var link = baseUrl + "?token=" + raw;
        var body = """
                Merhaba %s,

                Şifrenizi sıfırlamak için aşağıdaki bağlantıya %d dakika içinde tıklayın:
                %s

                Eğer talep sizden gelmediyse bu e-postayı yok sayabilirsiniz.
                """.formatted(user.getName(), ttlMinutes, link);

        try {
            mail.send(user.getEmail(), "Şifre Sıfırlama", body);
        } catch (Exception e) {
            log.warn("Mail sending failed for {}: {}", user.getEmail(), e.toString());
        }

        return raw;
    }

    @Transactional
    public void resetPassword(String rawToken, String newPassword) {
        String hash = sha256(rawToken);
        var prt = tokenRepo.findByTokenHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token geçersiz"));

        if (prt.getUsedAt() != null || Instant.now().isAfter(prt.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token süresi dolmuş veya kullanılmış");
        }

        var user = prt.getUser();
        user.setPassword(encoder.encode(newPassword));
        prt.setUsedAt(Instant.now());
        log.info("Password reset SUCCESS for userId={}, email='{}'", user.getId(), user.getEmail());
    }

    private String sha256(String s) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}

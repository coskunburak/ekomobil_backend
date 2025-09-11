package com.ekomobil.api.rest.auth;

import com.ekomobil.domain.dto.auth.AuthResponse;
import com.ekomobil.domain.dto.auth.ForgotPasswordRequest;
import com.ekomobil.domain.dto.auth.LoginRequest;
import com.ekomobil.domain.dto.auth.ResetPasswordRequest;
import com.ekomobil.domain.dto.auth.SignupRequest;
import com.ekomobil.domain.entity.User;
import com.ekomobil.security.JwtUtil;
import com.ekomobil.service.AuthService;
import com.ekomobil.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService service;
    private final JwtUtil jwt;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService service, JwtUtil jwt, PasswordResetService passwordResetService) {
        this.service = service; this.jwt = jwt; this.passwordResetService = passwordResetService;
    }

    @Value("${security.dev-auth.enabled:false}")
    private boolean devEnabled;

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgot(@Valid @RequestBody ForgotPasswordRequest req){
        String raw = passwordResetService.requestResetAndReturnRawIfDev(req.email(), devEnabled);
        if (devEnabled && raw != null) {
            return ResponseEntity.ok(Map.of("status", "ok", "debugRawToken", raw));
        }
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<Map<String, String>> reset(@Valid @RequestBody ResetPasswordRequest req){
        passwordResetService.resetPassword(req.token(), req.newPassword());
        return ResponseEntity.ok(Map.of("status", "password-updated"));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest req) {
        return ResponseEntity.ok(service.signup(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(service.login(req));
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication auth) {
        if (auth == null) {
            return Map.of("authenticated", false);
        }

        Long userId = null;
        String email = auth.getName();
        String name = null;

        Object principal = auth.getPrincipal();
        try {
            Class<?> upClass = Class.forName("com.ekomobil.security.UserPrincipal");
            if (upClass.isInstance(principal)) {
                var up = upClass.cast(principal);
                userId = (Long) upClass.getMethod("getId").invoke(up);
                email  = (String) upClass.getMethod("getEmail").invoke(up);
                try { name = (String) upClass.getMethod("getName").invoke(up); } catch (NoSuchMethodException ignored) {}
            }
        } catch (Exception ignored) {}

        List<String> authorities = auth.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).toList();

        return Map.of(
                "authenticated", true,
                "userId", userId,
                "email", email,
                "name", name,
                "authorities", authorities
        );
    }

    @GetMapping("/me/header")
    public ResponseEntity<?> meHeader(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.ok(Map.of("authenticated", false,
                    "error", "missing_or_invalid_authorization_header"));
        }

        String token = authHeader.substring(7);
        try {
            Long userId = jwt.getUserId(token);
            User u = service.me(userId);
            return ResponseEntity.ok(new AuthResponse(
                    "", u.getId(), u.getName(), u.getEmail()
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("authenticated", false,
                    "error", "invalid_token"));
        }
    }
}

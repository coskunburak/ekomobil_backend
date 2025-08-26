package com.ekomobil.api.rest.auth;

import com.ekomobil.domain.dto.auth.AuthResponse;
import com.ekomobil.domain.dto.auth.LoginRequest;
import com.ekomobil.domain.dto.auth.SignupRequest;
import com.ekomobil.domain.entity.User;
import com.ekomobil.security.JwtUtil;
import com.ekomobil.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService service;
    private final JwtUtil jwt;

    public AuthController(AuthService service, JwtUtil jwt) {
        this.service = service; this.jwt = jwt;
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest req) {
        return ResponseEntity.ok(service.signup(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(service.login(req));
    }

    // Basit /me: token'dan userId alıp döndürmek istersen
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwt.getUserId(token);
        User u = service.me(userId);
        return ResponseEntity.ok(new AuthResponse("",
                u.getId(), u.getName(), u.getEmail()));
    }
}

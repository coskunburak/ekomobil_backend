package com.ekomobil.service;

import com.ekomobil.domain.dto.auth.AuthResponse;
import com.ekomobil.domain.dto.auth.LoginRequest;
import com.ekomobil.domain.dto.auth.SignupRequest;
import com.ekomobil.domain.entity.User;
import com.ekomobil.repo.UserRepository;
import com.ekomobil.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    private final AuthenticationManager authManager;

    public AuthService(UserRepository repo, PasswordEncoder encoder, JwtUtil jwt, AuthenticationManager authManager) {
        this.repo = repo; this.encoder = encoder; this.jwt = jwt; this.authManager = authManager;
    }

    public AuthResponse signup(SignupRequest req) {
        if (repo.existsByEmail(req.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (repo.existsByUsername(req.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }

        User u = new User();
        u.setName(req.getName());
        u.setUsername(req.getUsername()); // <-- username zorunlu
        u.setEmail(req.getEmail());
        u.setPassword(encoder.encode(req.getPassword()));
        u = repo.save(u);

        return toAuthResponse(u);
    }

    public AuthResponse login(LoginRequest req) {
        // Email + password üzerinden doğrulama
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        User u = repo.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        return toAuthResponse(u);
    }

    /** (Opsiyonel) Username ile login desteği */
    public AuthResponse loginWithUsername(String username, String rawPassword) {
        User u = repo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        // AuthenticationManager tipik olarak username=email beklediğinden mail ile doğrulatıyoruz
        authManager.authenticate(new UsernamePasswordAuthenticationToken(u.getEmail(), rawPassword));
        return toAuthResponse(u);
    }

    public User me(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private AuthResponse toAuthResponse(User u) {
        String token = jwt.generate(u.getId(), u.getEmail());
        return new AuthResponse(token, u.getId(), u.getName(), u.getEmail());
    }
}

package com.ekomobil.service;

import com.ekomobil.domain.dto.auth.AuthResponse;
import com.ekomobil.domain.dto.auth.LoginRequest;
import com.ekomobil.domain.dto.auth.SignupRequest;
import com.ekomobil.domain.entity.User;
import com.ekomobil.repo.UserRepository;
import com.ekomobil.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        if (repo.existsByEmail(req.getEmail()))
            throw new IllegalArgumentException("Email already registered");

        User u = new User();
        u.setName(req.getName());
        u.setEmail(req.getEmail());
        u.setPassword(encoder.encode(req.getPassword()));
        u = repo.save(u);

        String token = jwt.generate(u.getId(), u.getEmail());
        return new AuthResponse(token, u.getId(), u.getName(), u.getEmail());
    }

    public AuthResponse login(LoginRequest req) {
        // Spring Security ile doğrulama
        authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        User u = repo.findByEmail(req.getEmail()).orElseThrow();
        String token = jwt.generate(u.getId(), u.getEmail());
        return new AuthResponse(token, u.getId(), u.getName(), u.getEmail());
    }

    public User me(Long id) {
        return repo.findById(id).orElseThrow();
    }
}

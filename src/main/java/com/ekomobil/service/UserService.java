package com.ekomobil.service;

import com.ekomobil.domain.dto.*;
import com.ekomobil.domain.entity.User;
import com.ekomobil.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public UserDto me(Long userId) {
        User u = repo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));
        return toDto(u);
    }

    @Transactional
    public UserDto updateMe(Long userId, UpdateProfileRequest req) {
        User u = repo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        repo.findByUsername(req.username()).ifPresent(other -> {
            if (!other.getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Kullanıcı adı zaten kullanılıyor.");
            }
        });

        repo.findByEmail(req.email()).ifPresent(other -> {
            if (!other.getId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email zaten kullanılıyor");
            }
        });

        u.setName(req.name());
        u.setUsername(req.username());
        u.setEmail(req.email());

        return toDto(u);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        User u = repo.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        if (!encoder.matches(req.currentPassword(), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mevcut şifre yanlış");
        }
        u.setPassword(encoder.encode(req.newPassword()));
    }

    private static UserDto toDto(User u) {
        return new UserDto(u.getId(), u.getName(), u.getUsername(), u.getEmail(),
                u.getCreatedAt(), u.getUpdatedAt());
    }
}

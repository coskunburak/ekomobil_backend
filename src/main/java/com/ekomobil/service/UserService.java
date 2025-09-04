// src/main/java/com/ekomobil/service/UserService.java
package com.ekomobil.service;

import com.ekomobil.domain.dto.UpdateProfileRequest;
import com.ekomobil.domain.dto.UserDto;
import com.ekomobil.domain.entity.Role;
import com.ekomobil.domain.entity.User;
import com.ekomobil.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    // ─────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public UserDto me(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));
        return toDto(u);
    }

    @Transactional
    public UserDto updateMe(Long userId, UpdateProfileRequest req) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        u.setName(req.name());
        u.setUsername(req.username());
        u.setEmail(req.email());

        User saved = userRepository.save(u);
        return toDto(saved);
    }

    @Transactional
    public void changePassword(Long userId, com.ekomobil.domain.dto.ChangePasswordRequest req) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Kullanıcı bulunamadı"));

        if (!encoder.matches(req.currentPassword(), u.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mevcut şifre yanlış");
        }
        u.setPassword(encoder.encode(req.newPassword()));
        userRepository.save(u);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEK MAPPER: Entity -> DTO (rolleri güvenli map'ler)
    private UserDto toDto(User u) {
        List<String> roles = (u.getRoles() == null)
                ? List.of()
                : u.getRoles().stream().map(Role::getName).toList();

        return new UserDto(
                u.getId(),
                u.getName(),
                u.getUsername(),
                u.getEmail(),
                u.getCreatedAt(),
                u.getUpdatedAt(),
                u.isEnabled(),
                roles
        );
    }
}

package com.ekomobil.domain.entity;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "password_reset_challenge")
@Getter @Setter
public class PasswordResetChallenge {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String email;
    @Column(name="code_hash", nullable = false, length = 64) private String codeHash;
    @Column(nullable = false, length = 24) private String salt;

    @Column(name="expires_at", nullable = false) private Instant expiresAt;

    @Column(nullable = false) private int attempts = 0;
    @Column(name="max_attempts", nullable = false) private int maxAttempts = 5;

    @Column(nullable = false) private boolean consumed = false;

    @Column(name="created_at", nullable = false) private Instant createdAt = Instant.now();
    @Column(name="consumed_at") private Instant consumedAt;
}

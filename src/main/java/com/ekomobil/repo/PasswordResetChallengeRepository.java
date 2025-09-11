package com.ekomobil.repo;

import com.ekomobil.domain.entity.PasswordResetChallenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetChallengeRepository
        extends JpaRepository<PasswordResetChallenge, Long> {

    Optional<PasswordResetChallenge> findFirstByEmailAndConsumedFalseOrderByExpiresAtDesc(String email);
}

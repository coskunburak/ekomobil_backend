package com.ekomobil.domain.dto;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;

public record UserDto(
        Long id,
        String name,
        String username,
        String email,
        Instant createdAt,
        Instant updatedAt
) {}

package com.ekomobil.domain.dto;

import java.time.Instant;

public record UserDto(
        Long id,
        String name,
        String username,
        String email,
        Instant createdAt,
        Instant updatedAt
) {}

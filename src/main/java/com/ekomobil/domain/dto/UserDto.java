package com.ekomobil.domain.dto;

import java.time.Instant;
import java.util.List;

public record UserDto(
        Long id,
        String name,
        String username,
        String email,
        Instant createdAt,
        Instant updatedAt,
        boolean enabled,
        List<String> roles
) {}

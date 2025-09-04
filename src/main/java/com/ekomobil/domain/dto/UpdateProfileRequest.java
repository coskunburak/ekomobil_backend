package com.ekomobil.domain.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.bind.annotation.GetMapping;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 80) String name,
        @NotBlank @Size(max = 40) String username,
        @Email @NotBlank String email
) {}

package com.ekomobil.domain.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerformPasswordResetRequest(
        @NotBlank String token,
        @NotBlank @Size(min=8, max=128) String newPassword
) {}

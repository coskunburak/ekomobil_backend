package com.ekomobil.domain.dto.auth;

public record VerifyResetCodeResponse(
        String resetToken,
        int expiresInSeconds
) {}

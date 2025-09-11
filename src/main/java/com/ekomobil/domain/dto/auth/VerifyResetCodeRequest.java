package com.ekomobil.domain.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VerifyResetCodeRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min=6, max=6) @Pattern(regexp="\\d{6}") String code
) {}

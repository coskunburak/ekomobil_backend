package com.ekomobil.domain.dto;

import jakarta.validation.constraints.*;

public record StopDto(
        Long id,
        @NotBlank @Size(max = 100) String name,
        @NotNull @DecimalMin(value = "-90") @DecimalMax(value = "90") Double lat,
        @NotNull @DecimalMin(value = "-180") @DecimalMax(value = "180") Double lon,
        @Size(max = 255) String description,
        @Size(max = 32) String code
) {}

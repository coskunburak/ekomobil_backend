package com.ekomobil.domain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record TelemetryRequest(
        @NotNull @DecimalMin(value="-90") @DecimalMax(value="90") Double lat,
        @NotNull @DecimalMin(value="-180") @DecimalMax(value="180") Double lon,
        @PositiveOrZero Double speed,
        @DecimalMin("0.0") @DecimalMax("360.0") Double heading,
        @NotNull String ts
) {}

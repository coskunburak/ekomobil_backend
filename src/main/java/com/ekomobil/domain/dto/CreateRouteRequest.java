package com.ekomobil.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRouteRequest(
        @NotBlank @Size(max=32) String code,
        @NotBlank @Size(max=120) String name,
        Boolean active
) {}

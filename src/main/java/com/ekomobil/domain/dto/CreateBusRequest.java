package com.ekomobil.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBusRequest(
        @NotBlank @Size(max=32) String code,
        @NotBlank @Size(max=20) String plate,
        Long routeId,
        Boolean active
) {}

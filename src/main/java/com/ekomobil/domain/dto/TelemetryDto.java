package com.ekomobil.domain.dto;

import java.time.OffsetDateTime;

public record TelemetryDto(
        String deviceId,
        OffsetDateTime ts,
        double lat,
        double lon,
        Double speed,
        Double heading
) {}

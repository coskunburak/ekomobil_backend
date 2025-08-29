package com.ekomobil.domain.dto;

import java.util.List;

public record RouteDetailDto(
        Long id,
        String code,
        String name,
        int direction,
        String directionName,
        String polyline,
        List<RouteDetailStopDto> stops
) {}

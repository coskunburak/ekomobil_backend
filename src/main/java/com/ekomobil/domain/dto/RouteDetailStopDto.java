package com.ekomobil.domain.dto;

import java.util.List;

public record RouteDetailStopDto(
        Long id,
        String name,
        String code,
        double lat,
        double lon,
        List<String> servedBy
) {}

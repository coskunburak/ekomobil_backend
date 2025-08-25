package com.ekomobil.domain.dto;

public record PositionDto(Long id, Long busId, double lat, double lon, Double speed, Double heading, String ts) {}

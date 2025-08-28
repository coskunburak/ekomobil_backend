package com.ekomobil.service;

import com.ekomobil.domain.dto.map.LineDto;
import com.ekomobil.domain.dto.map.StopDto;
import com.ekomobil.domain.entity.Route;
import com.ekomobil.domain.entity.RouteStop;
import com.ekomobil.domain.entity.Stop;
import com.ekomobil.repo.RouteRepository;
import com.ekomobil.repo.RouteStopRepository;
import com.ekomobil.repo.StopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MapService {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final RouteStopRepository routeStopRepository;

    public List<LineDto> getLines() {
        return routeRepository.findAll().stream()
                .map(this::toLineDto)
                .collect(Collectors.toList());
    }

    public List<StopDto> getStopsByLine(Long lineId) {
        return routeStopRepository.findByRouteIdOrderByOrderNo(lineId).stream()
                .map(rs -> toStopDto(rs.getStop(), rs.getOrderNo()))
                .collect(Collectors.toList());
    }

    public List<StopDto> getNearbyStops(double lat, double lng, double radiusMeters, int limit) {
        List<Stop> all = stopRepository.findAll();
        record Pair(Stop s, double d){}
        return all.stream()
                .map(s -> new Pair(s, haversine(lat, lng, s.getLat(), s.getLon())))
                .filter(p -> p.d <= radiusMeters)
                .sorted(Comparator.comparingDouble(p -> p.d))
                .limit(limit)
                .map(p -> toStopDto(p.s, null))
                .collect(Collectors.toList());
    }

    private LineDto toLineDto(Route r) {
        return LineDto.builder()
                .id(r.getId())
                .code(r.getCode())
                .name(r.getName())
                .color(r.getColor())
                .build();
    }

    private StopDto toStopDto(Stop s, Integer orderNo) {
        return StopDto.builder()
                .id(s.getId())
                .name(s.getName())
                .lat(s.getLat())
                .lon(s.getLon())
                .orderNo(orderNo)
                .build();
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371000d;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2)*Math.sin(dLon/2);
        return 2*R*Math.asin(Math.sqrt(a));
    }
}

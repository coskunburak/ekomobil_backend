package com.ekomobil.service;

import com.ekomobil.domain.dto.CreateRouteRequest;
import com.ekomobil.domain.dto.RouteDetailDto;
import com.ekomobil.domain.dto.RouteDetailStopDto;
import com.ekomobil.domain.dto.RouteDto;
import com.ekomobil.domain.entity.Route;
import com.ekomobil.domain.entity.RouteStop;
import com.ekomobil.error.NotFoundException;
import com.ekomobil.repo.RouteRepository;
import com.ekomobil.repo.RouteStopRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class RouteService {

    private final RouteRepository repo;
    private final RouteStopRepository routeStopRepository;

    public RouteService(RouteRepository repo, RouteStopRepository routeStopRepository) {
        this.repo = repo;
        this.routeStopRepository = routeStopRepository;
    }

    @Transactional(readOnly = true)
    public RouteDetailDto getDetail(Long routeId, Integer directionParam) {
        final int direction = (directionParam == null || directionParam != 1) ? 0 : 1;

        var route = repo.findById(routeId)
                .orElseThrow(() -> new NotFoundException("Rota bulunamadı: " + routeId));

        String polyline = route.getPolyline();

        List<RouteStop> ordered = routeStopRepository.findByRouteIdOrderByOrderNo(routeId);
        if (ordered.isEmpty()) {
            return new RouteDetailDto(
                    route.getId(),
                    route.getCode(),
                    route.getName(),
                    direction,
                    route.getName(),
                    polyline,
                    List.of()
            );
        }

        if (direction == 1) {
            ordered = new java.util.ArrayList<>(ordered);
            java.util.Collections.reverse(ordered);
        }

        List<RouteDetailStopDto> stops = ordered.stream().map(rs -> {
            var s = rs.getStop();
            var servedBy = routeStopRepository.findServedRouteCodesByStop(s.getId());
            return new RouteDetailStopDto(
                    s.getId(),
                    s.getName(),
                    s.getCode(),
                    s.getLat(),
                    s.getLon(),
                    servedBy
            );
        }).toList();

        return new RouteDetailDto(
                route.getId(),
                route.getCode(),
                route.getName(),
                direction,
                direction == 0
                        ? route.getName() + " → Otogar"
                        : "Otogar → " + route.getName(),
                polyline,
                stops
        );
    }

    @Transactional(readOnly = true)
    public Page<RouteDto> list(int page, int size) {
        Pageable p = PageRequest.of(page, size, Sort.by("id").descending());
        return repo.findAll(p).map(r -> new RouteDto(r.getId(), r.getCode(), r.getName(), r.isActive()));
    }

    @Transactional
    public RouteDto create(CreateRouteRequest req) {
        if (repo.existsByCode(req.code())) {
            throw new IllegalArgumentException("Rota kodu zaten kullanılıyor");
        }
        Route r = new Route();
        r.setCode(req.code());
        r.setName(req.name());
        r.setActive(req.active() == null || req.active());
        r = repo.save(r);
        return new RouteDto(r.getId(), r.getCode(), r.getName(), r.isActive());
    }

    @Transactional
    public RouteDto update(Long id, RouteDto dto) {
        Route r = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Rota bulunamadı: " + id));

        if (!Objects.equals(r.getCode(), dto.code())) {
            if (repo.existsByCode(dto.code())) {
                throw new IllegalArgumentException("Rota kodu zaten kullanılıyor");
            }
            r.setCode(dto.code());
        }

        r.setName(dto.name());
        r.setActive(dto.active());

        r = repo.save(r);
        return new RouteDto(r.getId(), r.getCode(), r.getName(), r.isActive());
    }

    @Transactional
    public void delete(Long id) {
        Route r = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Route not found: " + id));

        try {
            repo.delete(r);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Route is in use and cannot be deleted. Detach related records first.");
        }
    }
}

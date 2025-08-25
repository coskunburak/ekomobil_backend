package com.ekomobil.service;

import com.ekomobil.domain.dto.*;
import com.ekomobil.domain.entity.Route;
import com.ekomobil.repo.RouteRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RouteService {
    private final RouteRepository repo;
    public RouteService(RouteRepository repo){ this.repo = repo; }

    @Transactional(readOnly=true)
    public Page<RouteDto> list(int page, int size){
        Pageable p = PageRequest.of(page, size, Sort.by("id").descending());
        return repo.findAll(p).map(r -> new RouteDto(r.getId(), r.getCode(), r.getName(), r.isActive()));
    }

    @Transactional
    public RouteDto create(CreateRouteRequest req){
        if (repo.existsByCode(req.code())) throw new IllegalArgumentException("Route code already exists");
        Route r = new Route();
        r.setCode(req.code());
        r.setName(req.name());
        r.setActive(req.active() == null || req.active());
        r = repo.save(r);
        return new RouteDto(r.getId(), r.getCode(), r.getName(), r.isActive());
    }

}

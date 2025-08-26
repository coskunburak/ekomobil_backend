package com.ekomobil.service;

import com.ekomobil.domain.dto.CreateRouteRequest;
import com.ekomobil.domain.dto.RouteDto;
import com.ekomobil.domain.entity.Route;
import com.ekomobil.error.NotFoundException;
import com.ekomobil.repo.RouteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
public class RouteService {

    private final RouteRepository repo;

    public RouteService(RouteRepository repo){ this.repo = repo; }

    @Transactional(readOnly = true)
    public Page<RouteDto> list(int page, int size){
        Pageable p = PageRequest.of(page, size, Sort.by("id").descending());
        return repo.findAll(p).map(r -> new RouteDto(r.getId(), r.getCode(), r.getName(), r.isActive()));
    }

    @Transactional
    public RouteDto create(CreateRouteRequest req){
        if (repo.existsByCode(req.code())) {
            throw new IllegalArgumentException("Route code already exists");
        }
        Route r = new Route();
        r.setCode(req.code());
        r.setName(req.name());
        r.setActive(req.active() == null || req.active());
        r = repo.save(r);
        return new RouteDto(r.getId(), r.getCode(), r.getName(), r.isActive());
    }

    @Transactional
    public RouteDto update(Long id, RouteDto dto){
        Route r = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Route not found: " + id));

        if (!Objects.equals(r.getCode(), dto.code())) {
            if (repo.existsByCode(dto.code())) {
                throw new IllegalArgumentException("Route code already exists");
            }
            r.setCode(dto.code());
        }

        r.setName(dto.name());
        r.setActive(dto.active());

        r = repo.save(r);
        return new RouteDto(r.getId(), r.getCode(), r.getName(), r.isActive());
    }

    @Transactional
    public void delete(Long id){
        Route r = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Route not found: " + id));

        try {
            repo.delete(r);
        } catch (DataIntegrityViolationException ex) {
            // Örn. bu route'a bağlı Bus kayıtları varsa DB FK hatası verir
            throw new IllegalStateException("Route is in use and cannot be deleted. Detach related records first.");
        }
    }
}

package com.ekomobil.service;


import com.ekomobil.domain.dto.BusDto;
import com.ekomobil.domain.dto.CreateBusRequest;
import com.ekomobil.domain.entity.Bus;
import com.ekomobil.domain.entity.Route;
import com.ekomobil.error.NotFoundException;
import com.ekomobil.repo.BusRepository;
import com.ekomobil.repo.RouteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusService {
    private final BusRepository busRepo;
    private final RouteRepository routeRepo;

    public BusService(BusRepository busRepo, RouteRepository routeRepo) {
        this.busRepo = busRepo;
        this.routeRepo = routeRepo;
    }

    @Transactional
    public Page<BusDto> list(int page, int size) {
        Pageable p = PageRequest.of(page, size, Sort.by("id").descending());
        return busRepo.findAll(p).map(this::toDto);
    }

    @Transactional
    public BusDto create(CreateBusRequest req) {
        Bus b = new Bus();
        b.setCode(req.code());
        b.setPlate(req.plate());
        b.setActive(req.active() == null || req.active());

        if (req.routeId() != null) {
            Route r = routeRepo.findById(req.routeId())
                    .orElseThrow(() -> new NotFoundException("Rota bulunamadı: " + req.routeId()));
            b.setRoute(r);
        }

        b = busRepo.save(b);
        return toDto(b);
    }

    @Transactional
    public BusDto update(Long id, BusDto dto) {
        Bus b = busRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Otobüs bulunamadı: " + id));

        b.setCode(dto.code());
        b.setPlate(dto.plate());
        b.setActive(dto.active());

        if (dto.routeId() != null) {
            Route r = routeRepo.findById(dto.routeId())
                    .orElseThrow(() -> new NotFoundException("Rota bulunamadı: " + dto.routeId()));
            b.setRoute(r);
        } else {
            b.setRoute(null);
        }

        b = busRepo.save(b);
        return toDto(b);
    }

    @Transactional
    public void delete(Long id) {
        Bus b = busRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Otobüs bulunamadı: " + id));
        busRepo.delete(b);
    }

    private BusDto toDto(Bus b) {
        return new BusDto(
                b.getId(),
                b.getCode(),
                b.getPlate(),
                b.getRoute() != null ? b.getRoute().getId() : null,
                b.isActive()
        );
    }
}

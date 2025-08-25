package com.ekomobil.service;

import com.ekomobil.domain.dto.PositionDto;
import com.ekomobil.domain.dto.TelemetryRequest;
import com.ekomobil.domain.entity.Bus;
import com.ekomobil.domain.entity.VehiclePosition;
import com.ekomobil.repo.BusRepository;
import com.ekomobil.repo.VehiclePositionRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

@Service
public class TelemetryService {
    private final VehiclePositionRepository posRepo;
    private final BusRepository busRepo;
    private final SimpMessagingTemplate ws;

    public TelemetryService(VehiclePositionRepository posRepo, BusRepository busRepo, SimpMessagingTemplate ws){
        this.posRepo = posRepo; this.busRepo = busRepo; this.ws = ws;
    }

    @Transactional
    public PositionDto ingest(Long busId, TelemetryRequest req){
        Bus bus = busRepo.findById(busId).orElseThrow(() -> new IllegalArgumentException("Bus not found"));

        VehiclePosition vp = new VehiclePosition();
        vp.setBus(bus);
        vp.setLat(req.lat());
        vp.setLon(req.lon());
        vp.setSpeed(req.speed());
        vp.setHeading(req.heading());
        vp.setTs(OffsetDateTime.parse(req.ts()));
        vp = posRepo.save(vp);

        PositionDto dto = new PositionDto(vp.getId(), bus.getId(), vp.getLat(), vp.getLon(), vp.getSpeed(), vp.getHeading(), vp.getTs().toString());
        ws.convertAndSend("/topic/positions", dto);
        return dto;
    }
}

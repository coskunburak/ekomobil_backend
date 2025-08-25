package com.ekomobil.service;

import com.ekomobil.domain.dto.StopDto;
import com.ekomobil.domain.entity.Stop;
import com.ekomobil.error.NotFoundException;
import com.ekomobil.repo.StopRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StopService {
    private final StopRepository repo;

    public StopService(StopRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public Page<StopDto> list(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return repo.findAll(pageable).map(this::toDto);
    }

    @Transactional
    public StopDto create(StopDto dto) {
        Stop s = new Stop();
        s.setName(dto.name());
        s.setLat(dto.lat());
        s.setLon(dto.lon());
        s.setDescription(dto.description());
        s = repo.save(s);
        return toDto(s);
    }

    @Transactional
    public StopDto update(Long id, StopDto dto)
    {
        Stop s = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Stop not found: " + id));
        s.setName(dto.name());
        s.setLat(dto.lat());
        s.setLon(dto.lon());
        s.setDescription(dto.description());
        s = repo.save(s);
        return toDto(s);
    }

    @Transactional
    public void delete(Long id)
    {
        if(!repo.existsById(id))
        {
            throw new NotFoundException("Stop not found: " + id);
        }
        repo.deleteById(id);
    }
    private StopDto toDto(Stop s)
    {
        return new StopDto(s.getId(),s.getName(),s.getLat(), s.getLon(),s.getDescription());
    }
}

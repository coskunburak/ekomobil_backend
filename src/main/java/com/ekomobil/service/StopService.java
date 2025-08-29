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
        if (dto.code() != null && !dto.code().isBlank() && repo.existsByCode(dto.code())) {
            throw new IllegalArgumentException("Durak kodu zaten kullanılıyor: " + dto.code());
        }
        Stop s = new Stop();
        s.setName(dto.name());
        s.setLat(dto.lat());
        s.setLon(dto.lon());
        s.setDescription(dto.description());
        s.setCode(dto.code());
        s = repo.save(s);
        return toDto(s);
    }

    @Transactional
    public StopDto update(Long id, StopDto dto) {
        Stop s = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Durak bulunamadı: " + id));

        if (dto.code() != null && !dto.code().isBlank()) {
            repo.findByCode(dto.code()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("Stop code already exists: " + dto.code());
                }
            });
        }

        s.setName(dto.name());
        s.setLat(dto.lat());
        s.setLon(dto.lon());
        s.setDescription(dto.description());
        s.setCode(dto.code());
        s = repo.save(s);
        return toDto(s);
    }

    private StopDto toDto(Stop s) {
        return new StopDto(s.getId(), s.getName(), s.getLat(), s.getLon(), s.getDescription(), s.getCode());
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
}

package com.ekomobil.api.rest.publicapi;

import com.ekomobil.domain.dto.StopDto;
import com.ekomobil.service.StopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stops")
@Tag(name = "stop-controller")
public class StopController {

    private final StopService service;

    public StopController(StopService service) { this.service = service; }

    @Operation(summary = "Durakları sayfalı listele")
    @GetMapping
    public Page<StopDto> list(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        return service.list(page, size);
    }

    @Operation(summary = "Yeni durak oluştur")
    @PostMapping
    public ResponseEntity<StopDto> create(@Valid @RequestBody StopDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    @Operation(summary = "Mevcut durağı güncelle")
    @PutMapping("/{id}")
    public ResponseEntity<StopDto> update(@PathVariable Long id, @Valid @RequestBody StopDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @Operation(summary = "Durağı sil")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

package com.ekomobil.api.rest.publicapi;

import com.ekomobil.domain.dto.PositionDto;
import com.ekomobil.domain.dto.TelemetryRequest;
import com.ekomobil.service.TelemetryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {
    private final TelemetryService service;
    public TelemetryController(TelemetryService service){ this.service = service; }

    @Operation(summary = "Cihazdan konum telemetrisi al", description = "X-Device-Key ile yetkili cihaz gönderir.")
    @PostMapping
    public PositionDto ingest(@Valid @RequestBody TelemetryRequest req, Authentication auth){
        Long busId = (Long) auth.getPrincipal();
        return service.ingest(busId, req);
    }
}

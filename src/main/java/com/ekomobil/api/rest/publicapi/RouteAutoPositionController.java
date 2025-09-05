package com.ekomobil.api.rest.publicapi;

import com.ekomobil.service.RouteAutoPositionService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/routes")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class RouteAutoPositionController {

    private final RouteAutoPositionService svc;

    public record AutoPositionRequest(
            @Min(2) Integer spacingMeters,
            Boolean overwrite,
            Boolean onlyZero
    ) {}

    public record AutoPositionResult(
            long routeId,
            int stopCount,
            int updated,
            int skipped
    ) {}

    @PostMapping("/{routeId}/stops/autoposition")
    public AutoPositionResult autoposition(@PathVariable long routeId,
                                           @RequestBody(required = false) AutoPositionRequest req) {
        int spacing = (req != null && req.spacingMeters() != null) ? req.spacingMeters() : -1;
        boolean overwrite = req != null && Boolean.TRUE.equals(req.overwrite());
        boolean onlyZero = (req == null || req.onlyZero() == null) ? true : req.onlyZero();
        return svc.autoposition(routeId, spacing, overwrite, onlyZero);
    }
}

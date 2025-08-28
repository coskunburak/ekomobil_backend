package com.ekomobil.api.rest.publicapi;

import com.ekomobil.domain.dto.map.LineDto;
import com.ekomobil.domain.dto.map.StopDto;
import com.ekomobil.service.MapService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/map")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @Operation(summary = "Tüm hatlar")
    @GetMapping("/lines")
    public List<LineDto> lines() {
        return mapService.getLines();
    }

    @Operation(summary = "Hat üzerindeki duraklar")
    @GetMapping("/stops")
    public List<StopDto> stopsByLine(@RequestParam Long lineId) {
        return mapService.getStopsByLine(lineId);
    }

    @Operation(summary = "Yakındaki duraklar (metre)")
    @GetMapping("/stops/nearby")
    public List<StopDto> nearby(@RequestParam double lat,
                                @RequestParam double lng,
                                @RequestParam(defaultValue = "800") double radius,
                                @RequestParam(defaultValue = "50") int limit) {
        return mapService.getNearbyStops(lat, lng, radius, limit);
    }
}

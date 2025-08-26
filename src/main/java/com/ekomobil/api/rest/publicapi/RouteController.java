package com.ekomobil.api.rest.publicapi;

import com.ekomobil.domain.dto.CreateRouteRequest;
import com.ekomobil.domain.dto.RouteDto;
import com.ekomobil.service.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {

    private final RouteService service;

    public RouteController(RouteService service){ this.service = service; }

    @Operation(summary = "Rotaları listele")
    @GetMapping
    public Page<RouteDto> list(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size){
        return service.list(page, size);
    }

    @Operation(summary = "Yeni rota oluştur")
    @PostMapping
    public RouteDto create(@RequestBody @Valid CreateRouteRequest req){
        return service.create(req);
    }

    @Operation(summary = "Rota güncelle")
    @PutMapping("/{id}")
    public RouteDto update(@PathVariable Long id, @RequestBody @Valid RouteDto dto){
        return service.update(id, dto);
    }

    @Operation(summary = "Rota sil")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        service.delete(id);
    }
}

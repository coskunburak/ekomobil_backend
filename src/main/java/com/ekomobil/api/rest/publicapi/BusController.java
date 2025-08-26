package com.ekomobil.api.rest.publicapi;

import com.ekomobil.domain.dto.BusDto;
import com.ekomobil.domain.dto.CreateBusRequest;
import com.ekomobil.service.BusService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/buses")
public class BusController {

    private final BusService service;

    public BusController(BusService service){ this.service = service; }

    @Operation(summary = "Otobüsleri listele")
    @GetMapping
    public Page<BusDto> list(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size){
        return service.list(page, size);
    }

    @Operation(summary = "Yeni otobüs oluştur")
    @PostMapping
    public BusDto create(@RequestBody @Valid CreateBusRequest req){
        return service.create(req);
    }

    @Operation(summary = "Otobüs güncelle")
    @PutMapping("/{id}")
    public BusDto update(@PathVariable Long id, @RequestBody @Valid BusDto dto){
        return service.update(id, dto);
    }

    @Operation(summary = "Otobüs sil")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        service.delete(id);
    }
}

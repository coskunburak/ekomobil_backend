package com.ekomobil.api.rest.publicapi;

import com.ekomobil.domain.dto.SearchItemDto;
import com.ekomobil.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @Operation(summary = "Rota/Durak araması")
    @GetMapping
    public ResponseEntity<Map<String, Object>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit) {

        List<SearchItemDto> items = searchService.search(q, limit);

        return ResponseEntity.ok(Map.of("items", items));
    }
}

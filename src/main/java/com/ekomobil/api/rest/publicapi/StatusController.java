package com.ekomobil.api.rest.publicapi;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class StatusController
{
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status()
    {
        return ResponseEntity.ok(Map.of(
           "service", "ekomobil", "version", "1.8.0", "time", OffsetDateTime.now().toString()
        ));
    }
}

package com.reoagms.monitoring_service.controller;

import com.reoagms.monitoring_service.dto.TelemetryRequest;
import com.reoagms.monitoring_service.dto.TelemetryResponse;
import com.reoagms.monitoring_service.service.TelemetryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/telemetry")
@RequiredArgsConstructor
public class TelemetryController {

    private final TelemetryService service;

    @PostMapping
    public ResponseEntity<TelemetryResponse> record(@Valid @RequestBody TelemetryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.record(request));
    }

    @GetMapping
    public List<TelemetryResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/asset/{assetId}")
    public List<TelemetryResponse> getByAsset(@PathVariable UUID assetId) {
        return service.getByAsset(assetId);
    }

    @GetMapping("/asset/{assetId}/latest")
    public TelemetryResponse getLatestForAsset(@PathVariable UUID assetId) {
        return service.getLatestForAsset(assetId);
    }
}

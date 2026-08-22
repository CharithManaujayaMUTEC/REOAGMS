package com.reoagms.monitoring_service.controller;

import com.reoagms.monitoring_service.common.enums.MetricType;
import com.reoagms.monitoring_service.dto.SensorReadingRequest;
import com.reoagms.monitoring_service.dto.SensorReadingResponse;
import com.reoagms.monitoring_service.service.SensorReadingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Query params (facilityId, assetId, from, to) intentionally mirror the shape
 * Analytics Service uses when pulling historical readings across services.
 */
@RestController
@RequestMapping("/api/v1/sensor-readings")
@RequiredArgsConstructor
public class SensorReadingController {

    private final SensorReadingService service;

    @PostMapping
    public ResponseEntity<SensorReadingResponse> create(@Valid @RequestBody SensorReadingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public List<SensorReadingResponse> getAll(
            @RequestParam(required = false) UUID facilityId,
            @RequestParam(required = false) UUID assetId,
            @RequestParam(required = false) UUID sensorId,
            @RequestParam(required = false) MetricType metricType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return service.getAll(facilityId, assetId, sensorId, metricType, from, to);
    }

    @GetMapping("/{id}")
    public SensorReadingResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @GetMapping("/sensor/{sensorId}")
    public List<SensorReadingResponse> getBySensor(@PathVariable UUID sensorId) {
        return service.getBySensor(sensorId);
    }

    @GetMapping("/sensor/{sensorId}/latest")
    public SensorReadingResponse getLatestForSensor(@PathVariable UUID sensorId) {
        return service.getLatestForSensor(sensorId);
    }

    @GetMapping("/asset/{assetId}")
    public List<SensorReadingResponse> getByAsset(@PathVariable UUID assetId) {
        return service.getByAsset(assetId);
    }
}

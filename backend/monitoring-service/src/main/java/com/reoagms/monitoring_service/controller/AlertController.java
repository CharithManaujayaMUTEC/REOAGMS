package com.reoagms.monitoring_service.controller;

import com.reoagms.monitoring_service.common.enums.AlertStatus;
import com.reoagms.monitoring_service.dto.AlertRequest;
import com.reoagms.monitoring_service.dto.AlertResponse;
import com.reoagms.monitoring_service.service.AlertService;
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
 * Analytics Service uses when pulling alert data across services.
 */
@RestController
@RequestMapping("/api/v1/alerts")
@RequiredArgsConstructor
public class AlertController {

    private final AlertService service;

    @PostMapping
    public ResponseEntity<AlertResponse> create(@Valid @RequestBody AlertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public List<AlertResponse> getAll(
            @RequestParam(required = false) UUID facilityId,
            @RequestParam(required = false) UUID assetId,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return service.getAll(facilityId, assetId, status, from, to);
    }

    @GetMapping("/{id}")
    public AlertResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PutMapping("/{id}/acknowledge")
    public AlertResponse acknowledge(@PathVariable UUID id) {
        return service.acknowledge(id);
    }

    @PutMapping("/{id}/resolve")
    public AlertResponse resolve(@PathVariable UUID id) {
        return service.resolve(id);
    }
}

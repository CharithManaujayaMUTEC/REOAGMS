package com.reoagms.asset_service.sensor.controller;

import com.reoagms.asset_service.common.enums.SensorStatus;
import com.reoagms.asset_service.common.enums.SensorType;
import com.reoagms.asset_service.common.model.PageResponse;
import com.reoagms.asset_service.sensor.dto.SensorRequest;
import com.reoagms.asset_service.sensor.dto.SensorResponse;
import com.reoagms.asset_service.sensor.service.SensorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @PostMapping
    public ResponseEntity<SensorResponse> create(@Valid @RequestBody SensorRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(sensorService.create(request));

    }

    @GetMapping
    public PageResponse<SensorResponse> getAll(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        return PageResponse.from(sensorService.getAll(pageable));

    }

    @GetMapping("/search")
    public PageResponse<SensorResponse> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) SensorType type,
            @RequestParam(required = false) SensorStatus status,
            @RequestParam(required = false) UUID assetId,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {

        return PageResponse.from(sensorService.search(name, type, status, assetId, pageable));

    }

    @GetMapping("/{id}")
    public SensorResponse getById(@PathVariable UUID id) {

        return sensorService.getById(id);

    }

    @GetMapping("/asset/{assetId}")
    public List<SensorResponse> getByAsset(@PathVariable UUID assetId) {

        return sensorService.getByAsset(assetId);

    }

    @PutMapping("/{id}")
    public SensorResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody SensorRequest request) {

        return sensorService.update(id, request);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {

        sensorService.delete(id);

        return ResponseEntity.noContent().build();

    }

}

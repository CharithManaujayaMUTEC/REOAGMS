package com.reoagms.maintenance_service.controller;

import com.reoagms.maintenance_service.dto.MaintenanceHistoryRequest;
import com.reoagms.maintenance_service.dto.MaintenanceHistoryResponse;
import com.reoagms.maintenance_service.service.MaintenanceHistoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance-history")
@RequiredArgsConstructor
public class MaintenanceHistoryController {

    private final MaintenanceHistoryService service;

    @PostMapping
    public MaintenanceHistoryResponse create(@Valid @RequestBody MaintenanceHistoryRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<MaintenanceHistoryResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/asset/{assetId}")
    public List<MaintenanceHistoryResponse> getByAsset(@PathVariable UUID assetId) {
        return service.getByAsset(assetId);
    }

    @GetMapping("/work-order/{workOrderId}")
    public List<MaintenanceHistoryResponse> getByWorkOrder(@PathVariable UUID workOrderId) {
        return service.getByWorkOrder(workOrderId);
    }
}

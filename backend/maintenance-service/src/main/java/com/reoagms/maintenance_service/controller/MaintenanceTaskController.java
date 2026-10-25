package com.reoagms.maintenance_service.controller;

import com.reoagms.maintenance_service.dto.MaintenanceTaskRequest;
import com.reoagms.maintenance_service.dto.MaintenanceTaskResponse;
import com.reoagms.maintenance_service.service.MaintenanceTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/maintenance-tasks")
@RequiredArgsConstructor
public class MaintenanceTaskController {

    private final MaintenanceTaskService service;

    @PostMapping
    public MaintenanceTaskResponse create(@Valid @RequestBody MaintenanceTaskRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<MaintenanceTaskResponse> getAll(
            @RequestParam(required = false) UUID workOrderId) {
        return workOrderId != null ? service.getByWorkOrder(workOrderId) : service.getAll();
    }

    @GetMapping("/{id}")
    public MaintenanceTaskResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public MaintenanceTaskResponse update(@PathVariable UUID id, @Valid @RequestBody MaintenanceTaskRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PutMapping("/{id}/start")
    public MaintenanceTaskResponse start(@PathVariable UUID id) {
        return service.start(id);
    }

    @PutMapping("/{id}/complete")
    public MaintenanceTaskResponse complete(@PathVariable UUID id) {
        return service.complete(id);
    }
}

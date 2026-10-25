package com.reoagms.maintenance_service.controller;

import com.reoagms.maintenance_service.dto.WorkOrderRequest;
import com.reoagms.maintenance_service.dto.WorkOrderResponse;
import com.reoagms.maintenance_service.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService service;

    @PostMapping
    public WorkOrderResponse create(@Valid @RequestBody WorkOrderRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<WorkOrderResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public WorkOrderResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public WorkOrderResponse update(@PathVariable UUID id, @Valid @RequestBody WorkOrderRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }

    @PutMapping("/{id}/assign/{technicianId}")
    public WorkOrderResponse assign(@PathVariable UUID id, @PathVariable UUID technicianId) {
        return service.assignTechnician(id, technicianId);
    }

    @PutMapping("/{id}/start")
    public WorkOrderResponse start(@PathVariable UUID id) {
        return service.start(id);
    }

    @PutMapping("/{id}/complete")
    public WorkOrderResponse complete(@PathVariable UUID id) {
        return service.complete(id);
    }
}

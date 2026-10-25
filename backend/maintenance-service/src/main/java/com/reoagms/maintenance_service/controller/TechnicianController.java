package com.reoagms.maintenance_service.controller;

import com.reoagms.maintenance_service.dto.TechnicianRequest;
import com.reoagms.maintenance_service.dto.TechnicianResponse;
import com.reoagms.maintenance_service.service.TechnicianService;
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

/**
 * Only keep this controller (and the Technician entity) if the team's
 * agreed design has Maintenance Service owning technician records.
 * If technicians live in Identity/User Service, delete this module
 * and keep technicianId as a plain UUID reference elsewhere.
 */
@RestController
@RequestMapping("/api/v1/technicians")
@RequiredArgsConstructor
public class TechnicianController {

    private final TechnicianService service;

    @PostMapping
    public TechnicianResponse create(@Valid @RequestBody TechnicianRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<TechnicianResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public TechnicianResponse getById(@PathVariable UUID id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public TechnicianResponse update(@PathVariable UUID id, @Valid @RequestBody TechnicianRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}

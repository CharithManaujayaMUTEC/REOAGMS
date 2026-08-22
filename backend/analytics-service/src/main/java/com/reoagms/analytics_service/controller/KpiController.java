package com.reoagms.analytics_service.controller;

import com.reoagms.analytics_service.common.model.ApiResponse;
import com.reoagms.analytics_service.dto.KpiRequest;
import com.reoagms.analytics_service.dto.KpiResponse;
import com.reoagms.analytics_service.service.KpiService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/kpis")
@RequiredArgsConstructor
public class KpiController {

    private final KpiService service;

    @PostMapping
    public ResponseEntity<ApiResponse<KpiResponse>> create(@Valid @RequestBody KpiRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("KPI created", service.create(request)));
    }

    @GetMapping
    public ApiResponse<List<KpiResponse>> getAll() {
        return ApiResponse.success("KPIs retrieved", service.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<KpiResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success("KPI retrieved", service.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<KpiResponse> update(
            @PathVariable UUID id, @Valid @RequestBody KpiRequest request) {
        return ApiResponse.success("KPI updated", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

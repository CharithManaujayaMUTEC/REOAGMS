package com.reoagms.analytics_service.controller;

import com.reoagms.analytics_service.common.model.ApiResponse;
import com.reoagms.analytics_service.dto.DashboardRequest;
import com.reoagms.analytics_service.dto.DashboardResponse;
import com.reoagms.analytics_service.service.DashboardService;
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
@RequestMapping("/api/v1/dashboards")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @PostMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> create(@Valid @RequestBody DashboardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dashboard created", service.create(request)));
    }

    @GetMapping
    public ApiResponse<List<DashboardResponse>> getAll() {
        return ApiResponse.success("Dashboards retrieved", service.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<DashboardResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success("Dashboard retrieved", service.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<DashboardResponse> update(
            @PathVariable UUID id, @Valid @RequestBody DashboardRequest request) {
        return ApiResponse.success("Dashboard updated", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

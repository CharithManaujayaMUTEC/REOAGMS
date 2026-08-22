package com.reoagms.monitoring_service.controller;

import com.reoagms.monitoring_service.common.model.ApiResponse;
import com.reoagms.monitoring_service.dto.AlertRuleRequest;
import com.reoagms.monitoring_service.dto.AlertRuleResponse;
import com.reoagms.monitoring_service.service.AlertRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/alert-rules")
@RequiredArgsConstructor
public class AlertRuleController {

    private final AlertRuleService service;

    @PostMapping
    public ResponseEntity<ApiResponse<AlertRuleResponse>> create(@Valid @RequestBody AlertRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Alert rule created", service.create(request)));
    }

    @GetMapping
    public ApiResponse<List<AlertRuleResponse>> getAll() {
        return ApiResponse.success("Alert rules retrieved", service.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<AlertRuleResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success("Alert rule retrieved", service.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<AlertRuleResponse> update(
            @PathVariable UUID id, @Valid @RequestBody AlertRuleRequest request) {
        return ApiResponse.success("Alert rule updated", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

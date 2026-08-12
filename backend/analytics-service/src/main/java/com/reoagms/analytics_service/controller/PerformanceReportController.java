package com.reoagms.analytics_service.controller;

import com.reoagms.analytics_service.common.enums.ExportFormat;
import com.reoagms.analytics_service.common.model.ApiResponse;
import com.reoagms.analytics_service.dto.PerformanceReportRequest;
import com.reoagms.analytics_service.dto.PerformanceReportResponse;
import com.reoagms.analytics_service.service.PerformanceReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/performance-reports")
@RequiredArgsConstructor
public class PerformanceReportController {

    private final PerformanceReportService service;

    @PostMapping
    public ResponseEntity<ApiResponse<PerformanceReportResponse>> create(
            @Valid @RequestBody PerformanceReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Performance report generated", service.create(request)));
    }

    @GetMapping
    public ApiResponse<List<PerformanceReportResponse>> getAll() {
        return ApiResponse.success("Performance reports retrieved", service.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<PerformanceReportResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success("Performance report retrieved", service.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<PerformanceReportResponse> update(
            @PathVariable UUID id, @Valid @RequestBody PerformanceReportRequest request) {
        return ApiResponse.success("Performance report regenerated", service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "CSV") ExportFormat format) {
        byte[] content = service.exportCsv(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("performance-report-" + id + ".csv")
                .build());
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}

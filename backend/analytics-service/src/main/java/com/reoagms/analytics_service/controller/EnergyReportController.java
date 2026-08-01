package com.reoagms.analytics_service.controller;

import com.reoagms.analytics_service.common.enums.ExportFormat;
import com.reoagms.analytics_service.common.model.ApiResponse;
import com.reoagms.analytics_service.dto.EnergyReportRequest;
import com.reoagms.analytics_service.dto.EnergyReportResponse;
import com.reoagms.analytics_service.service.EnergyReportService;
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
@RequestMapping("/api/v1/energy-reports")
@RequiredArgsConstructor
public class EnergyReportController {

    private final EnergyReportService service;

    @PostMapping
    public ResponseEntity<ApiResponse<EnergyReportResponse>> create(
            @Valid @RequestBody EnergyReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Energy report generated", service.create(request)));
    }

    @GetMapping
    public ApiResponse<List<EnergyReportResponse>> getAll() {
        return ApiResponse.success("Energy reports retrieved", service.getAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<EnergyReportResponse> getById(@PathVariable UUID id) {
        return ApiResponse.success("Energy report retrieved", service.getById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse<EnergyReportResponse> update(
            @PathVariable UUID id, @Valid @RequestBody EnergyReportRequest request) {
        return ApiResponse.success("Energy report regenerated", service.update(id, request));
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
                .filename("energy-report-" + id + ".csv")
                .build());
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}

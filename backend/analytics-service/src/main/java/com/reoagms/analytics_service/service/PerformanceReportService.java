package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.dto.PerformanceReportRequest;
import com.reoagms.analytics_service.dto.PerformanceReportResponse;

import java.util.List;
import java.util.UUID;

public interface PerformanceReportService {
    PerformanceReportResponse create(PerformanceReportRequest request);
    List<PerformanceReportResponse> getAll();
    PerformanceReportResponse getById(UUID id);
    PerformanceReportResponse update(UUID id, PerformanceReportRequest request);
    void delete(UUID id);
    byte[] exportCsv(UUID id);
}

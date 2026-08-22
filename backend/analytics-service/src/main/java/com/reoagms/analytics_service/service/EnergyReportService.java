package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.dto.EnergyReportRequest;
import com.reoagms.analytics_service.dto.EnergyReportResponse;

import java.util.List;
import java.util.UUID;

public interface EnergyReportService {
    EnergyReportResponse create(EnergyReportRequest request);
    List<EnergyReportResponse> getAll();
    EnergyReportResponse getById(UUID id);
    EnergyReportResponse update(UUID id, EnergyReportRequest request);
    void delete(UUID id);
    byte[] exportCsv(UUID id);
}

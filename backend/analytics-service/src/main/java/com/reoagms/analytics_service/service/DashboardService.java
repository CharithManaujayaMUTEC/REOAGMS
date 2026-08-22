package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.dto.DashboardRequest;
import com.reoagms.analytics_service.dto.DashboardResponse;

import java.util.List;
import java.util.UUID;

public interface DashboardService {
    DashboardResponse create(DashboardRequest request);
    List<DashboardResponse> getAll();
    DashboardResponse getById(UUID id);
    DashboardResponse update(UUID id, DashboardRequest request);
    void delete(UUID id);
}

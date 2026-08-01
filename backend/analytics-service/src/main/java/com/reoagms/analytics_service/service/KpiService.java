package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.dto.KpiRequest;
import com.reoagms.analytics_service.dto.KpiResponse;

import java.util.List;
import java.util.UUID;

public interface KpiService {
    KpiResponse create(KpiRequest request);
    List<KpiResponse> getAll();
    KpiResponse getById(UUID id);
    KpiResponse update(UUID id, KpiRequest request);
    void delete(UUID id);
}

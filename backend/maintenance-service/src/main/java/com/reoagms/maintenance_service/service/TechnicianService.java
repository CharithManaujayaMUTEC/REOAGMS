package com.reoagms.maintenance_service.service;

import com.reoagms.maintenance_service.dto.TechnicianRequest;
import com.reoagms.maintenance_service.dto.TechnicianResponse;

import java.util.List;
import java.util.UUID;

public interface TechnicianService {

    TechnicianResponse create(TechnicianRequest request);

    List<TechnicianResponse> getAll();

    TechnicianResponse getById(UUID id);

    TechnicianResponse update(UUID id, TechnicianRequest request);

    void delete(UUID id);
}

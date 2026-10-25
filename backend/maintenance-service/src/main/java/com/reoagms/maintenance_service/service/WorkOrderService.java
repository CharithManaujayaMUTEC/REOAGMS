package com.reoagms.maintenance_service.service;

import com.reoagms.maintenance_service.dto.WorkOrderRequest;
import com.reoagms.maintenance_service.dto.WorkOrderResponse;

import java.util.List;
import java.util.UUID;

public interface WorkOrderService {

    WorkOrderResponse create(WorkOrderRequest request);

    List<WorkOrderResponse> getAll();

    WorkOrderResponse getById(UUID id);

    WorkOrderResponse update(UUID id, WorkOrderRequest request);

    void delete(UUID id);

    WorkOrderResponse assignTechnician(UUID id, UUID technicianId);

    WorkOrderResponse start(UUID id);

    WorkOrderResponse complete(UUID id);
}

package com.reoagms.maintenance_service.service;

import com.reoagms.maintenance_service.dto.MaintenanceTaskRequest;
import com.reoagms.maintenance_service.dto.MaintenanceTaskResponse;

import java.util.List;
import java.util.UUID;

public interface MaintenanceTaskService {

    MaintenanceTaskResponse create(MaintenanceTaskRequest request);

    List<MaintenanceTaskResponse> getAll();

    List<MaintenanceTaskResponse> getByWorkOrder(UUID workOrderId);

    MaintenanceTaskResponse getById(UUID id);

    MaintenanceTaskResponse update(UUID id, MaintenanceTaskRequest request);

    void delete(UUID id);

    MaintenanceTaskResponse start(UUID id);

    MaintenanceTaskResponse complete(UUID id);
}

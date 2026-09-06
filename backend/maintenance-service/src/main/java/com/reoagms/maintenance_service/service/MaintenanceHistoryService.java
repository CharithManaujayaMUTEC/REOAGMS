package com.reoagms.maintenance_service.service;

import com.reoagms.maintenance_service.dto.MaintenanceHistoryRequest;
import com.reoagms.maintenance_service.dto.MaintenanceHistoryResponse;

import java.util.List;
import java.util.UUID;

public interface MaintenanceHistoryService {

    MaintenanceHistoryResponse create(MaintenanceHistoryRequest request);

    List<MaintenanceHistoryResponse> getAll();

    List<MaintenanceHistoryResponse> getByAsset(UUID assetId);

    List<MaintenanceHistoryResponse> getByWorkOrder(UUID workOrderId);
}

package com.reoagms.maintenance_service.mapper;

import com.reoagms.maintenance_service.dto.MaintenanceHistoryRequest;
import com.reoagms.maintenance_service.dto.MaintenanceHistoryResponse;
import com.reoagms.maintenance_service.model.MaintenanceHistory;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceHistoryMapper {

    public MaintenanceHistory toEntity(MaintenanceHistoryRequest request) {
        return MaintenanceHistory.builder()
                .assetId(request.getAssetId())
                .workOrderId(request.getWorkOrderId())
                .technicianId(request.getTechnicianId())
                .maintenanceType(request.getMaintenanceType())
                .summary(request.getSummary())
                .performedAt(request.getPerformedAt())
                .notes(request.getNotes())
                .build();
    }

    public MaintenanceHistoryResponse toResponse(MaintenanceHistory e) {
        return MaintenanceHistoryResponse.builder()
                .id(e.getId())
                .assetId(e.getAssetId())
                .workOrderId(e.getWorkOrderId())
                .technicianId(e.getTechnicianId())
                .maintenanceType(e.getMaintenanceType())
                .summary(e.getSummary())
                .performedAt(e.getPerformedAt())
                .notes(e.getNotes())
                .build();
    }
}

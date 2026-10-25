package com.reoagms.maintenance_service.mapper;

import com.reoagms.maintenance_service.common.enums.WorkOrderStatus;
import com.reoagms.maintenance_service.dto.WorkOrderRequest;
import com.reoagms.maintenance_service.dto.WorkOrderResponse;
import com.reoagms.maintenance_service.model.WorkOrder;
import org.springframework.stereotype.Component;

@Component
public class WorkOrderMapper {

    public WorkOrder toEntity(WorkOrderRequest request) {
        return WorkOrder.builder()
                .assetId(request.getAssetId())
                .technicianId(request.getTechnicianId())
                .title(request.getTitle())
                .description(request.getDescription())
                .maintenanceType(request.getMaintenanceType())
                .status(WorkOrderStatus.CREATED)
                .scheduledAt(request.getScheduledAt())
                .build();
    }

    public WorkOrderResponse toResponse(WorkOrder e) {
        return WorkOrderResponse.builder()
                .id(e.getId())
                .assetId(e.getAssetId())
                .technicianId(e.getTechnicianId())
                .title(e.getTitle())
                .description(e.getDescription())
                .maintenanceType(e.getMaintenanceType())
                .status(e.getStatus())
                .scheduledAt(e.getScheduledAt())
                .completedAt(e.getCompletedAt())
                .build();
    }
}

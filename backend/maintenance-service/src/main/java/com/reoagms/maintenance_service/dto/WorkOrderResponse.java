package com.reoagms.maintenance_service.dto;

import com.reoagms.maintenance_service.common.enums.MaintenanceType;
import com.reoagms.maintenance_service.common.enums.WorkOrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class WorkOrderResponse {

    private UUID id;
    private UUID assetId;
    private UUID technicianId;
    private String title;
    private String description;
    private MaintenanceType maintenanceType;
    private WorkOrderStatus status;
    private LocalDateTime scheduledAt;
    private LocalDateTime completedAt;
}

package com.reoagms.maintenance_service.dto;

import com.reoagms.maintenance_service.common.enums.MaintenanceType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MaintenanceHistoryResponse {

    private UUID id;
    private UUID assetId;
    private UUID workOrderId;
    private UUID technicianId;
    private MaintenanceType maintenanceType;
    private String summary;
    private LocalDateTime performedAt;
    private String notes;
}

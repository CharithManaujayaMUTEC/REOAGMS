package com.reoagms.maintenance_service.dto;

import com.reoagms.maintenance_service.common.enums.MaintenanceType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class MaintenanceHistoryRequest {

    @NotNull
    private UUID assetId;

    @NotNull
    private UUID workOrderId;

    private UUID technicianId;

    @NotNull
    private MaintenanceType maintenanceType;

    private String summary;

    @NotNull
    private LocalDateTime performedAt;

    private String notes;
}

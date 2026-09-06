package com.reoagms.maintenance_service.dto;

import com.reoagms.maintenance_service.common.enums.MaintenanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class WorkOrderRequest {

    @NotNull
    private UUID assetId;

    private UUID technicianId;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private MaintenanceType maintenanceType;

    private LocalDateTime scheduledAt;
}

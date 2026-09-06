package com.reoagms.maintenance_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class MaintenanceTaskRequest {

    @NotNull
    private UUID workOrderId;

    @NotBlank
    private String title;

    private String description;

    private UUID technicianId;
}

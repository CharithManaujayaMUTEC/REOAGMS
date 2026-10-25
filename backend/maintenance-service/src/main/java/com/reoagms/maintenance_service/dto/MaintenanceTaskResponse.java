package com.reoagms.maintenance_service.dto;

import com.reoagms.maintenance_service.common.enums.TaskStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class MaintenanceTaskResponse {

    private UUID id;
    private UUID workOrderId;
    private String title;
    private String description;
    private UUID technicianId;
    private TaskStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}

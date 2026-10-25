package com.reoagms.maintenance_service.mapper;

import com.reoagms.maintenance_service.common.enums.TaskStatus;
import com.reoagms.maintenance_service.dto.MaintenanceTaskRequest;
import com.reoagms.maintenance_service.dto.MaintenanceTaskResponse;
import com.reoagms.maintenance_service.model.MaintenanceTask;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceTaskMapper {

    public MaintenanceTask toEntity(MaintenanceTaskRequest request) {
        return MaintenanceTask.builder()
                .workOrderId(request.getWorkOrderId())
                .title(request.getTitle())
                .description(request.getDescription())
                .technicianId(request.getTechnicianId())
                .status(TaskStatus.PENDING)
                .build();
    }

    public MaintenanceTaskResponse toResponse(MaintenanceTask e) {
        return MaintenanceTaskResponse.builder()
                .id(e.getId())
                .workOrderId(e.getWorkOrderId())
                .title(e.getTitle())
                .description(e.getDescription())
                .technicianId(e.getTechnicianId())
                .status(e.getStatus())
                .startedAt(e.getStartedAt())
                .completedAt(e.getCompletedAt())
                .build();
    }
}

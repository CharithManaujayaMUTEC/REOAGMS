package com.reoagms.maintenance_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregated, report-friendly data. Deliberately avoids returning
 * raw entity collections so the API stays cheap for dashboards.
 */
@Data
@Builder
public class MaintenanceReportResponse {

    private long totalWorkOrders;
    private long completedWorkOrders;
    private long overdueWorkOrders;
    private long preventiveCount;
    private long correctiveCount;

    private Map<UUID, Long> workOrdersByAsset;
    private Map<UUID, Long> workOrdersByTechnician;

    private List<WorkOrderResponse> overdueList;
}

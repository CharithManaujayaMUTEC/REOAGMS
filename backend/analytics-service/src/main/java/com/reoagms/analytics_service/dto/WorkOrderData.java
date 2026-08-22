package com.reoagms.analytics_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class WorkOrderData {
    private UUID id;
    private UUID facilityId;
    private UUID assetId;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private BigDecimal downtimeHours;
}

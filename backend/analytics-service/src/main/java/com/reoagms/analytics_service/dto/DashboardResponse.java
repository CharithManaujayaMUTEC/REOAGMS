package com.reoagms.analytics_service.dto;

import com.reoagms.analytics_service.common.enums.DashboardType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class DashboardResponse {
    private UUID id;
    private String name;
    private String description;
    private DashboardType dashboardType;
    private UUID ownerUserId;
    private Integer defaultRangeDays;
    private Boolean active;
    private String layoutConfiguration;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

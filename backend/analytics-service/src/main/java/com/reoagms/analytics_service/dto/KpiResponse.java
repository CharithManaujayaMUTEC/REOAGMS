package com.reoagms.analytics_service.dto;

import com.reoagms.analytics_service.common.enums.KpiStatus;
import com.reoagms.analytics_service.common.enums.KpiType;
import com.reoagms.analytics_service.common.enums.ScopeType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class KpiResponse {
    private UUID id;
    private String name;
    private KpiType kpiType;
    private ScopeType scopeType;
    private UUID scopeId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private BigDecimal actualValue;
    private BigDecimal targetValue;
    private String unit;
    private KpiStatus kpiStatus;
    private LocalDateTime calculatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

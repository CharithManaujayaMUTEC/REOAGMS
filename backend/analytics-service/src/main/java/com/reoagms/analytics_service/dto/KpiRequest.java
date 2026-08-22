package com.reoagms.analytics_service.dto;

import com.reoagms.analytics_service.common.enums.KpiType;
import com.reoagms.analytics_service.common.enums.ScopeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class KpiRequest {
    @NotBlank
    @Size(max = 120)
    private String name;

    @NotNull
    private KpiType kpiType;

    @NotNull
    private ScopeType scopeType;

    private UUID scopeId;

    @NotNull
    private LocalDateTime periodStart;

    @NotNull
    private LocalDateTime periodEnd;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal actualValue;

    @DecimalMin("0.0")
    private BigDecimal targetValue;

    @NotBlank
    @Size(max = 30)
    private String unit;
}

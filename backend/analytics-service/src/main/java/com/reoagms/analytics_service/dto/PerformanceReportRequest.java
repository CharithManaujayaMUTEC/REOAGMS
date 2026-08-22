package com.reoagms.analytics_service.dto;

import com.reoagms.analytics_service.common.enums.ScopeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PerformanceReportRequest {
    @NotBlank
    @Size(max = 120)
    private String name;

    @NotNull
    private ScopeType scopeType;

    private UUID scopeId;

    @NotNull
    private LocalDateTime periodStart;

    @NotNull
    private LocalDateTime periodEnd;
}

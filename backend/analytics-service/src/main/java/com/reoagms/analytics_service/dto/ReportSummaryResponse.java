package com.reoagms.analytics_service.dto;

import com.reoagms.analytics_service.common.enums.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReportSummaryResponse {
    private UUID id;
    private String reportType;
    private String name;
    private UUID scopeId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime generatedAt;
    private ReportStatus status;
}

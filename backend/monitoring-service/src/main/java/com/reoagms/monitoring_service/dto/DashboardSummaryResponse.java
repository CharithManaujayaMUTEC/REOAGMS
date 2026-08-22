package com.reoagms.monitoring_service.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class DashboardSummaryResponse {
    private long totalReadingsLast24h;
    private long openAlerts;
    private long acknowledgedAlerts;
    private long resolvedAlerts;
    private Map<String, Long> alertsBySeverity;
    private long onlineDevices;
    private long offlineDevices;
    private long warningDevices;
}

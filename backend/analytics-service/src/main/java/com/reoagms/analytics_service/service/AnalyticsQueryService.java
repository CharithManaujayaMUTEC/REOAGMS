package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.common.enums.AggregationPeriod;
import com.reoagms.analytics_service.dto.DashboardSummaryResponse;
import com.reoagms.analytics_service.dto.ReportCollectionResponse;
import com.reoagms.analytics_service.dto.StatisticsResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AnalyticsQueryService {
    DashboardSummaryResponse getDashboard(
            UUID facilityId, UUID assetId, LocalDateTime from, LocalDateTime to);

    StatisticsResponse getStatistics(
            UUID facilityId,
            UUID assetId,
            LocalDateTime from,
            LocalDateTime to,
            AggregationPeriod groupBy);

    ReportCollectionResponse getReports();
}

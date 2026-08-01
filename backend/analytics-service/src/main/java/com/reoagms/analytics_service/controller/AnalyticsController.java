package com.reoagms.analytics_service.controller;

import com.reoagms.analytics_service.common.enums.AggregationPeriod;
import com.reoagms.analytics_service.common.model.ApiResponse;
import com.reoagms.analytics_service.dto.DashboardSummaryResponse;
import com.reoagms.analytics_service.dto.ReportCollectionResponse;
import com.reoagms.analytics_service.dto.StatisticsResponse;
import com.reoagms.analytics_service.service.AnalyticsQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsQueryService service;

    @GetMapping("/dashboard")
    public ApiResponse<DashboardSummaryResponse> dashboard(
            @RequestParam(required = false) UUID facilityId,
            @RequestParam(required = false) UUID assetId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        LocalDateTime effectiveTo = to == null ? LocalDateTime.now() : to;
        LocalDateTime effectiveFrom = from == null ? effectiveTo.minusDays(1) : from;
        return ApiResponse.success("Dashboard summary calculated",
                service.getDashboard(facilityId, assetId, effectiveFrom, effectiveTo));
    }

    @GetMapping("/reports")
    public ApiResponse<ReportCollectionResponse> reports() {
        return ApiResponse.success("Reports retrieved", service.getReports());
    }

    @GetMapping("/statistics")
    public ApiResponse<StatisticsResponse> statistics(
            @RequestParam(required = false) UUID facilityId,
            @RequestParam(required = false) UUID assetId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "DAY") AggregationPeriod groupBy) {
        LocalDateTime effectiveTo = to == null ? LocalDateTime.now() : to;
        LocalDateTime effectiveFrom = from == null ? effectiveTo.minusDays(7) : from;
        return ApiResponse.success("Statistics calculated",
                service.getStatistics(facilityId, assetId, effectiveFrom, effectiveTo, groupBy));
    }
}

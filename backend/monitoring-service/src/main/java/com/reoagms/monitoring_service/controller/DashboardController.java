package com.reoagms.monitoring_service.controller;

import com.reoagms.monitoring_service.common.model.ApiResponse;
import com.reoagms.monitoring_service.dto.DashboardSummaryResponse;
import com.reoagms.monitoring_service.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService service;

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> getSummary() {
        return ApiResponse.success("Dashboard summary retrieved", service.getSummary());
    }
}

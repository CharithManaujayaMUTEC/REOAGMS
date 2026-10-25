package com.reoagms.maintenance_service.controller;

import com.reoagms.maintenance_service.dto.MaintenanceReportResponse;
import com.reoagms.maintenance_service.service.MaintenanceReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/maintenance-reports")
@RequiredArgsConstructor
public class MaintenanceReportController {

    private final MaintenanceReportService service;

    @GetMapping("/summary")
    public MaintenanceReportResponse summary() {
        return service.getSummary();
    }
}

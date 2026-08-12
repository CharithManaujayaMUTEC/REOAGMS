package com.reoagms.analytics_service.repository;

import com.reoagms.analytics_service.model.PerformanceReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PerformanceReportRepository extends JpaRepository<PerformanceReport, UUID> {
}

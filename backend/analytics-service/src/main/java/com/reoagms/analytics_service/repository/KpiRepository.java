package com.reoagms.analytics_service.repository;

import com.reoagms.analytics_service.model.Kpi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface KpiRepository extends JpaRepository<Kpi, UUID> {
}

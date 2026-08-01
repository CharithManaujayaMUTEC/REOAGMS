package com.reoagms.analytics_service.repository;

import com.reoagms.analytics_service.model.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DashboardRepository extends JpaRepository<Dashboard, UUID> {
}

package com.reoagms.monitoring_service.repository;

import com.reoagms.monitoring_service.common.enums.AlertSeverity;
import com.reoagms.monitoring_service.common.enums.AlertStatus;
import com.reoagms.monitoring_service.model.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID>, JpaSpecificationExecutor<Alert> {

    long countByStatus(AlertStatus status);

    long countBySeverity(AlertSeverity severity);
}

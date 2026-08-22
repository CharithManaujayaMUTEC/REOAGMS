package com.reoagms.monitoring_service.repository;

import com.reoagms.monitoring_service.common.enums.MetricType;
import com.reoagms.monitoring_service.model.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRuleRepository extends JpaRepository<AlertRule, UUID> {

    List<AlertRule> findByEnabledTrueAndMetricTypeAndAssetId(MetricType metricType, UUID assetId);

    List<AlertRule> findByEnabledTrueAndMetricTypeAndAssetIdIsNull(MetricType metricType);

    List<AlertRule> findByAssetId(UUID assetId);
}

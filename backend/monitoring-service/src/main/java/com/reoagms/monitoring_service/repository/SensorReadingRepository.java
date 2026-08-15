package com.reoagms.monitoring_service.repository;

import com.reoagms.monitoring_service.model.SensorReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SensorReadingRepository
        extends JpaRepository<SensorReading, UUID>, JpaSpecificationExecutor<SensorReading> {

    List<SensorReading> findBySensorId(UUID sensorId);

    List<SensorReading> findByAssetId(UUID assetId);

    Page<SensorReading> findAll(org.springframework.data.jpa.domain.Specification<SensorReading> spec, Pageable pageable);

    Optional<SensorReading> findFirstBySensorIdOrderByTimestampDesc(UUID sensorId);

    List<SensorReading> findByAssetIdAndMetricTypeOrderByTimestampDesc(
            UUID assetId, com.reoagms.monitoring_service.common.enums.MetricType metricType);

    long countByTimestampAfter(LocalDateTime after);
}

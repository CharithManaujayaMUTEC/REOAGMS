package com.reoagms.monitoring_service.repository;

import com.reoagms.monitoring_service.common.enums.DeviceStatus;
import com.reoagms.monitoring_service.model.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TelemetryRepository extends JpaRepository<Telemetry, UUID> {

    List<Telemetry> findByAssetId(UUID assetId);

    Optional<Telemetry> findFirstByAssetIdOrderByLastSeenAtDesc(UUID assetId);

    long countByStatus(DeviceStatus status);
}

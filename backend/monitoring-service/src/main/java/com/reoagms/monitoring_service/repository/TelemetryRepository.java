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

    /** Counts assets whose most recent heartbeat has the given status (not every historical row). */
    @org.springframework.data.jpa.repository.Query("""
            select count(t) from Telemetry t where t.status = :status
              and t.lastSeenAt = (select max(t2.lastSeenAt) from Telemetry t2 where t2.assetId = t.assetId)""")
    long countLatestByStatus(@org.springframework.data.repository.query.Param("status") DeviceStatus status);
}

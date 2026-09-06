package com.reoagms.maintenance_service.repository;

import com.reoagms.maintenance_service.model.MaintenanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MaintenanceHistoryRepository extends JpaRepository<MaintenanceHistory, UUID> {

    List<MaintenanceHistory> findByAssetId(UUID assetId);

    List<MaintenanceHistory> findByWorkOrderId(UUID workOrderId);
}

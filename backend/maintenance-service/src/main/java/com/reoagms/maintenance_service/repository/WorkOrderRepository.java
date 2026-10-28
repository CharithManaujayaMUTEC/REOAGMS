package com.reoagms.maintenance_service.repository;

import com.reoagms.maintenance_service.common.enums.WorkOrderStatus;
import com.reoagms.maintenance_service.model.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface WorkOrderRepository extends JpaRepository<WorkOrder, UUID> {

    List<WorkOrder> findByAssetId(UUID assetId);

    boolean existsByDescriptionContaining(String fragment);

    boolean existsByAssetIdAndMaintenanceTypeAndStatusIn(UUID assetId, com.reoagms.maintenance_service.common.enums.MaintenanceType type, java.util.Collection<WorkOrderStatus> statuses);

    List<WorkOrder> findByTechnicianId(UUID technicianId);

    List<WorkOrder> findByStatus(WorkOrderStatus status);

    List<WorkOrder> findByScheduledAtBeforeAndStatusNot(LocalDateTime cutoff, WorkOrderStatus status);
}

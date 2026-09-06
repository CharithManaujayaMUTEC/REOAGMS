package com.reoagms.maintenance_service.repository;

import com.reoagms.maintenance_service.model.MaintenanceTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, UUID> {

    List<MaintenanceTask> findByWorkOrderId(UUID workOrderId);

    List<MaintenanceTask> findByTechnicianId(UUID technicianId);
}

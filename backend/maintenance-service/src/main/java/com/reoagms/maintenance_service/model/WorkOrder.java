package com.reoagms.maintenance_service.model;

import com.reoagms.maintenance_service.common.enums.MaintenanceType;
import com.reoagms.maintenance_service.common.enums.WorkOrderStatus;
import com.reoagms.maintenance_service.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "work_orders")
public class WorkOrder extends BaseEntity {

    @Column(nullable = false)
    private UUID assetId;

    private UUID technicianId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceType maintenanceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderStatus status;

    private LocalDateTime scheduledAt;

    private LocalDateTime completedAt;
}

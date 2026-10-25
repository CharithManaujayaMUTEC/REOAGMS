package com.reoagms.maintenance_service.model;

import com.reoagms.maintenance_service.common.enums.TechnicianAvailability;
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

/**
 * Only create/use this entity if the agreed system design says the
 * Maintenance Service owns technician records. Otherwise treat
 * technicianId (used on WorkOrder / MaintenanceTask) as a UUID
 * reference into the Identity/User Service and drop this entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "technicians")
public class Technician extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String specialization;

    @Column(length = 30)
    private String phone;

    @Column(length = 150)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TechnicianAvailability availability;
}

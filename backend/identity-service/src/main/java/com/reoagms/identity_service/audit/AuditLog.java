package com.reoagms.identity_service.audit;

import com.reoagms.identity_service.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Immutable record of an important user or operational event (FR-16). */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {
    @Column(nullable = false)
    private String actor;
    @Column(nullable = false)
    private String action;
    @Column(name = "entity_type")
    private String entityType;
    @Column(length = 1000)
    private String details;
}

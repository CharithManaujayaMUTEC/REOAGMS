package com.reoagms.analytics_service.model;

import com.reoagms.analytics_service.common.enums.DashboardType;
import com.reoagms.analytics_service.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "dashboards")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "dashboard_type", nullable = false, length = 40)
    private DashboardType dashboardType;

    @Column(name = "owner_user_id")
    private UUID ownerUserId;

    @Column(name = "default_range_days", nullable = false)
    private Integer defaultRangeDays;

    @Column(nullable = false)
    private Boolean active;

    @Lob
    @Column(name = "layout_configuration")
    private String layoutConfiguration;
}

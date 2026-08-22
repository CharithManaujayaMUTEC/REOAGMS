package com.reoagms.analytics_service.model;

import com.reoagms.analytics_service.common.enums.ReportStatus;
import com.reoagms.analytics_service.common.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "energy_reports", indexes = {
        @Index(name = "idx_energy_report_facility", columnList = "facility_id"),
        @Index(name = "idx_energy_report_asset", columnList = "asset_id"),
        @Index(name = "idx_energy_report_period", columnList = "period_start,period_end")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyReport extends BaseEntity {

    @Column(nullable = false, length = 120)
    private String name;

    @Column(name = "facility_id")
    private UUID facilityId;

    @Column(name = "asset_id")
    private UUID assetId;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "total_energy", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalEnergy;

    @Column(name = "average_energy", nullable = false, precision = 19, scale = 4)
    private BigDecimal averageEnergy;

    @Column(name = "peak_energy", nullable = false, precision = 19, scale = 4)
    private BigDecimal peakEnergy;

    @Column(name = "expected_energy", nullable = false, precision = 19, scale = 4)
    private BigDecimal expectedEnergy;

    @Column(name = "variance_percentage", nullable = false, precision = 9, scale = 4)
    private BigDecimal variancePercentage;

    @Column(nullable = false, length = 20)
    private String unit;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false, length = 20)
    private ReportStatus reportStatus;
}

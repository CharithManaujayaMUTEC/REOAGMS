package com.reoagms.analytics_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class EnergyReportRequest {
    @NotBlank
    @Size(max = 120)
    private String name;

    private UUID facilityId;
    private UUID assetId;

    @NotNull
    private LocalDateTime periodStart;

    @NotNull
    private LocalDateTime periodEnd;

    private BigDecimal expectedEnergy;
}

package com.reoagms.analytics_service.dto;

import com.reoagms.analytics_service.common.enums.DashboardType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class DashboardRequest {
    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    private DashboardType dashboardType;

    private UUID ownerUserId;

    @NotNull
    @Min(1)
    @Max(365)
    private Integer defaultRangeDays;

    @NotNull
    private Boolean active;

    private String layoutConfiguration;
}

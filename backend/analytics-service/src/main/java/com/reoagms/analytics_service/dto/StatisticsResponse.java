package com.reoagms.analytics_service.dto;

import com.reoagms.analytics_service.common.enums.AggregationPeriod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class StatisticsResponse {
    private UUID facilityId;
    private UUID assetId;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private AggregationPeriod groupedBy;
    private BigDecimal totalEnergy;
    private BigDecimal averageEnergy;
    private BigDecimal minimumEnergy;
    private BigDecimal maximumEnergy;
    private long readingCount;
    private List<TimeSeriesPoint> series;
}

package com.reoagms.analytics_service.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TimeSeriesPoint {
    private LocalDateTime period;
    private BigDecimal value;
    private String unit;
}

package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.common.enums.AggregationPeriod;
import com.reoagms.analytics_service.dto.AlertData;
import com.reoagms.analytics_service.dto.AssetData;
import com.reoagms.analytics_service.dto.EnergyMetrics;
import com.reoagms.analytics_service.dto.PerformanceMetrics;
import com.reoagms.analytics_service.dto.SensorReadingData;
import com.reoagms.analytics_service.dto.TimeSeriesPoint;
import com.reoagms.analytics_service.dto.WorkOrderData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsCalculationServiceTest {

    private AnalyticsCalculationService service;
    private LocalDateTime from;
    private LocalDateTime to;

    @BeforeEach
    void setUp() {
        service = new AnalyticsCalculationService();
        from = LocalDateTime.of(2026, 1, 1, 0, 0);
        to = from.plusHours(10);
    }

    @Test
    void calculatesEnergyAndNormalizesUnitsToKilowattHours() {
        SensorReadingData first = reading(from.plusHours(1), "500", "kWh");
        SensorReadingData second = reading(from.plusHours(2), "1", "MWh");
        AssetData asset = asset("100", "ACTIVE");

        EnergyMetrics result = service.calculateEnergy(
                List.of(first, second), List.of(asset), from, to, null);

        assertThat(result.getTotalEnergy()).isEqualByComparingTo("1500.0000");
        assertThat(result.getAverageEnergy()).isEqualByComparingTo("750.0000");
        assertThat(result.getPeakEnergy()).isEqualByComparingTo("1000.0000");
        assertThat(result.getExpectedEnergy()).isEqualByComparingTo("1000.0000");
        assertThat(result.getVariancePercentage()).isEqualByComparingTo("50.0000");
        assertThat(result.getUnit()).isEqualTo("kWh");
    }

    @Test
    void returnsZeroValuesForEmptyReadings() {
        EnergyMetrics result = service.calculateEnergy(List.of(), List.of(), from, to, null);

        assertThat(result.getTotalEnergy()).isEqualByComparingTo("0.0000");
        assertThat(result.getAverageEnergy()).isEqualByComparingTo("0.0000");
        assertThat(result.getVariancePercentage()).isEqualByComparingTo("0.0000");
    }

    @Test
    void calculatesAvailabilityUtilizationAndMaintenanceCompletion() {
        AssetData active = asset("100", "ACTIVE");
        AssetData inactive = asset("100", "OFFLINE");
        EnergyMetrics energy = service.calculateEnergy(
                List.of(reading(from.plusHours(1), "1000", "kWh")),
                List.of(active, inactive), from, to, null);

        WorkOrderData completed = workOrder("COMPLETED", "2");
        WorkOrderData open = workOrder("IN_PROGRESS", "3");

        PerformanceMetrics result = service.calculatePerformance(
                energy,
                List.of(active, inactive),
                List.of(new AlertData()),
                List.of(completed, open),
                from,
                to);

        assertThat(result.getAvailabilityPercentage()).isEqualByComparingTo("75.0000");
        assertThat(result.getUtilizationPercentage()).isEqualByComparingTo("50.0000");
        assertThat(result.getMaintenanceCompletionRate()).isEqualByComparingTo("50.0000");
        assertThat(result.getOpenWorkOrderCount()).isEqualTo(1);
    }

    @Test
    void groupsReadingsByDay() {
        List<TimeSeriesPoint> result = service.groupReadings(
                List.of(
                        reading(LocalDateTime.of(2026, 1, 1, 1, 0), "10", "kWh"),
                        reading(LocalDateTime.of(2026, 1, 1, 4, 0), "15", "kWh"),
                        reading(LocalDateTime.of(2026, 1, 2, 1, 0), "7", "kWh")),
                AggregationPeriod.DAY);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getValue()).isEqualByComparingTo("25.0000");
        assertThat(result.get(1).getValue()).isEqualByComparingTo("7.0000");
    }

    private SensorReadingData reading(LocalDateTime timestamp, String value, String unit) {
        SensorReadingData reading = new SensorReadingData();
        reading.setTimestamp(timestamp);
        reading.setValue(new BigDecimal(value));
        reading.setUnit(unit);
        reading.setMetricType("ENERGY_GENERATION");
        return reading;
    }

    private AssetData asset(String ratedCapacity, String status) {
        AssetData asset = new AssetData();
        asset.setRatedCapacity(new BigDecimal(ratedCapacity));
        asset.setStatus(status);
        return asset;
    }

    private WorkOrderData workOrder(String status, String downtimeHours) {
        WorkOrderData order = new WorkOrderData();
        order.setStatus(status);
        order.setDowntimeHours(new BigDecimal(downtimeHours));
        return order;
    }
}

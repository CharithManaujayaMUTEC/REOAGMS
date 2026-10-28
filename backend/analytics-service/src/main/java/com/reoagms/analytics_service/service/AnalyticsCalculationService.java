package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.common.enums.AggregationPeriod;
import com.reoagms.analytics_service.dto.AlertData;
import com.reoagms.analytics_service.dto.AssetData;
import com.reoagms.analytics_service.dto.EnergyMetrics;
import com.reoagms.analytics_service.dto.PerformanceMetrics;
import com.reoagms.analytics_service.dto.SensorReadingData;
import com.reoagms.analytics_service.dto.TimeSeriesPoint;
import com.reoagms.analytics_service.dto.WorkOrderData;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AnalyticsCalculationService {

    private static final int SCALE = 4;
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public EnergyMetrics calculateEnergy(
            List<SensorReadingData> readings,
            List<AssetData> assets,
            LocalDateTime from,
            LocalDateTime to,
            BigDecimal requestedExpectedEnergy) {
        // Energy per hour: average power of each sensor within the hour, summed across sensors.
        List<BigDecimal> values = new ArrayList<>(energyByBucket(readings, AggregationPeriod.HOUR).values());

        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = values.isEmpty()
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(values.size()), SCALE, RoundingMode.HALF_UP);
        BigDecimal peak = values.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);

        BigDecimal expected = requestedExpectedEnergy != null
                ? requestedExpectedEnergy
                : expectedEnergy(assets, from, to);
        BigDecimal variance = expected.signum() == 0
                ? BigDecimal.ZERO
                : total.subtract(expected)
                        .divide(expected, SCALE + 2, RoundingMode.HALF_UP)
                        .multiply(ONE_HUNDRED);

        return EnergyMetrics.builder()
                .totalEnergy(scale(total))
                .averageEnergy(scale(average))
                .peakEnergy(scale(peak))
                .expectedEnergy(scale(expected))
                .variancePercentage(scale(variance))
                .unit("kWh")
                .build();
    }

    public PerformanceMetrics calculatePerformance(
            EnergyMetrics energy,
            List<AssetData> assets,
            List<AlertData> alerts,
            List<WorkOrderData> workOrders,
            LocalDateTime from,
            LocalDateTime to) {
        int assetCount = Math.max(assets.size(), 1);
        BigDecimal scheduledHours = durationHours(from, to).multiply(BigDecimal.valueOf(assetCount));
        BigDecimal downtime = workOrders.stream()
                .map(WorkOrderData::getDowntimeHours)
                .filter(value -> value != null && value.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .min(scheduledHours);

        BigDecimal availability = scheduledHours.signum() == 0
                ? BigDecimal.ZERO
                : scheduledHours.subtract(downtime)
                        .divide(scheduledHours, SCALE + 2, RoundingMode.HALF_UP)
                        .multiply(ONE_HUNDRED);

        BigDecimal capacityFactor = energy.getExpectedEnergy().signum() == 0
                ? BigDecimal.ZERO
                : energy.getTotalEnergy()
                        .divide(energy.getExpectedEnergy(), SCALE + 2, RoundingMode.HALF_UP)
                        .multiply(ONE_HUNDRED);
        capacityFactor = clampPercentage(capacityFactor);

        long operationalAssets = assets.stream()
                .filter(asset -> isOperational(asset.getStatus()))
                .count();
        BigDecimal utilization = assets.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(operationalAssets)
                        .divide(BigDecimal.valueOf(assets.size()), SCALE + 2, RoundingMode.HALF_UP)
                        .multiply(ONE_HUNDRED);

        long completed = workOrders.stream().filter(order -> isCompleted(order.getStatus())).count();
        long open = workOrders.size() - completed;
        BigDecimal completionRate = workOrders.isEmpty()
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(completed)
                        .divide(BigDecimal.valueOf(workOrders.size()), SCALE + 2, RoundingMode.HALF_UP)
                        .multiply(ONE_HUNDRED);

        return PerformanceMetrics.builder()
                .availabilityPercentage(scale(clampPercentage(availability)))
                .efficiencyPercentage(scale(capacityFactor))
                .capacityFactorPercentage(scale(capacityFactor))
                .utilizationPercentage(scale(clampPercentage(utilization)))
                .downtimeHours(scale(downtime))
                .alertCount(alerts.size())
                .maintenanceCount(workOrders.size())
                .openWorkOrderCount(open)
                .maintenanceCompletionRate(scale(completionRate))
                .build();
    }

    public List<TimeSeriesPoint> groupReadings(
            List<SensorReadingData> readings, AggregationPeriod aggregationPeriod) {
        Map<LocalDateTime, BigDecimal> grouped = energyByBucket(readings, aggregationPeriod);

        List<TimeSeriesPoint> points = new ArrayList<>();
        grouped.forEach((period, value) -> points.add(TimeSeriesPoint.builder()
                .period(period)
                .value(scale(value))
                .unit("kWh")
                .build()));
        return points;
    }


    /**
     * Converts raw readings into energy (kWh) per time bucket. Power readings (kW/MW/W) are averaged per sensor
     * within the bucket and multiplied by the bucket length; readings already expressed as energy are summed.
     */
    private Map<LocalDateTime, BigDecimal> energyByBucket(List<SensorReadingData> readings, AggregationPeriod period) {
        Map<LocalDateTime, Map<UUID, List<SensorReadingData>>> grouped = new java.util.TreeMap<>();
        readings.stream()
                .filter(r -> r.getTimestamp() != null && r.getValue() != null)
                .forEach(r -> grouped
                        .computeIfAbsent(bucket(r.getTimestamp(), period), k -> new LinkedHashMap<>())
                        .computeIfAbsent(r.getSensorId(), k -> new ArrayList<>())
                        .add(r));

        Map<LocalDateTime, BigDecimal> result = new LinkedHashMap<>();
        grouped.forEach((bucketStart, bySensor) -> {
            BigDecimal hours = bucketHours(period, bucketStart);
            BigDecimal energy = BigDecimal.ZERO;
            for (List<SensorReadingData> sensorReadings : bySensor.values()) {
                if (isPowerUnit(sensorReadings.get(0).getUnit())) {
                    BigDecimal sum = sensorReadings.stream().map(this::toKilowatts).reduce(BigDecimal.ZERO, BigDecimal::add);
                    BigDecimal avg = sum.divide(BigDecimal.valueOf(sensorReadings.size()), SCALE + 2, RoundingMode.HALF_UP);
                    energy = energy.add(avg.multiply(hours));
                } else {
                    energy = energy.add(sensorReadings.stream().map(this::toKilowattHours).reduce(BigDecimal.ZERO, BigDecimal::add));
                }
            }
            result.put(bucketStart, energy);
        });
        return result;
    }

    private BigDecimal bucketHours(AggregationPeriod period, LocalDateTime start) {
        return switch (period) {
            case HOUR -> BigDecimal.ONE;
            case DAY -> BigDecimal.valueOf(24);
            case WEEK -> BigDecimal.valueOf(24 * 7);
            case MONTH -> BigDecimal.valueOf(24L * start.toLocalDate().lengthOfMonth());
        };
    }

    private boolean isPowerUnit(String unit) {
        if (unit == null) return false;
        String u = unit.toUpperCase(Locale.ROOT).replace(" ", "");
        return u.equals("KW") || u.equals("MW") || u.equals("W") || u.equals("GW");
    }

    private BigDecimal toKilowatts(SensorReadingData reading) {
        String u = reading.getUnit().toUpperCase(Locale.ROOT).replace(" ", "");
        return switch (u) {
            case "W" -> reading.getValue().divide(BigDecimal.valueOf(1000), SCALE + 2, RoundingMode.HALF_UP);
            case "MW" -> reading.getValue().multiply(BigDecimal.valueOf(1000));
            case "GW" -> reading.getValue().multiply(BigDecimal.valueOf(1_000_000));
            default -> reading.getValue();
        };
    }

    public BigDecimal normalizeReading(SensorReadingData reading) {
        return scale(toKilowattHours(reading));
    }

    private BigDecimal expectedEnergy(List<AssetData> assets, LocalDateTime from, LocalDateTime to) {
        BigDecimal ratedCapacity = assets.stream()
                .map(AssetData::getRatedCapacity)
                .filter(value -> value != null && value.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return ratedCapacity.multiply(durationHours(from, to));
    }

    private BigDecimal durationHours(LocalDateTime from, LocalDateTime to) {
        BigDecimal seconds = BigDecimal.valueOf(Duration.between(from, to).toSeconds());
        return seconds.divide(BigDecimal.valueOf(3600), SCALE + 2, RoundingMode.HALF_UP);
    }

    private BigDecimal toKilowattHours(SensorReadingData reading) {
        BigDecimal value = reading.getValue();
        String unit = reading.getUnit() == null ? "KWH" : reading.getUnit().toUpperCase(Locale.ROOT).replace(" ", "");
        return switch (unit) {
            case "WH" -> value.divide(BigDecimal.valueOf(1000), SCALE + 2, RoundingMode.HALF_UP);
            case "MWH" -> value.multiply(BigDecimal.valueOf(1000));
            case "GWH" -> value.multiply(BigDecimal.valueOf(1_000_000));
            default -> value;
        };
    }

    private LocalDateTime bucket(LocalDateTime timestamp, AggregationPeriod period) {
        return switch (period) {
            case HOUR -> timestamp.withMinute(0).withSecond(0).withNano(0);
            case DAY -> timestamp.toLocalDate().atStartOfDay();
            case WEEK -> timestamp.toLocalDate()
                    .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .atStartOfDay();
            case MONTH -> timestamp.toLocalDate().withDayOfMonth(1).atStartOfDay();
        };
    }

    private boolean isOperational(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.toUpperCase(Locale.ROOT);
        return normalized.equals("ACTIVE") || normalized.equals("OPERATIONAL") || normalized.equals("RUNNING");
    }

    private boolean isCompleted(String status) {
        if (status == null) {
            return false;
        }
        String normalized = status.toUpperCase(Locale.ROOT);
        return normalized.equals("COMPLETED") || normalized.equals("CLOSED") || normalized.equals("DONE");
    }

    private BigDecimal clampPercentage(BigDecimal value) {
        return value.max(BigDecimal.ZERO).min(ONE_HUNDRED);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}

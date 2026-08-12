package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.common.enums.AggregationPeriod;
import com.reoagms.analytics_service.dto.AlertData;
import com.reoagms.analytics_service.dto.AssetData;
import com.reoagms.analytics_service.dto.DashboardSummaryResponse;
import com.reoagms.analytics_service.dto.EnergyMetrics;
import com.reoagms.analytics_service.dto.PerformanceMetrics;
import com.reoagms.analytics_service.dto.ReportCollectionResponse;
import com.reoagms.analytics_service.dto.ReportSummaryResponse;
import com.reoagms.analytics_service.dto.SensorReadingData;
import com.reoagms.analytics_service.dto.StatisticsResponse;
import com.reoagms.analytics_service.dto.TimeSeriesPoint;
import com.reoagms.analytics_service.dto.WorkOrderData;
import com.reoagms.analytics_service.model.EnergyReport;
import com.reoagms.analytics_service.model.PerformanceReport;
import com.reoagms.analytics_service.repository.EnergyReportRepository;
import com.reoagms.analytics_service.repository.PerformanceReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {

    private final ExternalDataService externalDataService;
    private final AnalyticsCalculationService calculationService;
    private final AnalyticsValidationService validationService;
    private final EnergyReportRepository energyReportRepository;
    private final PerformanceReportRepository performanceReportRepository;

    @Override
    public DashboardSummaryResponse getDashboard(
            UUID facilityId, UUID assetId, LocalDateTime from, LocalDateTime to) {
        validateQuery(facilityId, assetId, from, to);

        List<AssetData> assets = externalDataService.getAssets(facilityId, assetId);
        List<SensorReadingData> readings = externalDataService.getReadings(facilityId, assetId, from, to);
        List<AlertData> alerts = externalDataService.getAlerts(facilityId, assetId, from, to);
        List<WorkOrderData> workOrders = externalDataService.getWorkOrders(facilityId, assetId, from, to);
        EnergyMetrics energy = calculationService.calculateEnergy(readings, assets, from, to, null);
        PerformanceMetrics performance = calculationService.calculatePerformance(
                energy, assets, alerts, workOrders, from, to);

        long activeAlerts = alerts.stream().filter(this::isActiveAlert).count();
        AggregationPeriod trendPeriod = Duration.between(from, to).toDays() <= 2
                ? AggregationPeriod.HOUR
                : AggregationPeriod.DAY;

        return DashboardSummaryResponse.builder()
                .facilityId(facilityId)
                .assetId(assetId)
                .periodStart(from)
                .periodEnd(to)
                .totalEnergy(energy.getTotalEnergy())
                .averageEnergy(energy.getAverageEnergy())
                .peakEnergy(energy.getPeakEnergy())
                .energyUnit(energy.getUnit())
                .availabilityPercentage(performance.getAvailabilityPercentage())
                .efficiencyPercentage(performance.getEfficiencyPercentage())
                .capacityFactorPercentage(performance.getCapacityFactorPercentage())
                .utilizationPercentage(performance.getUtilizationPercentage())
                .activeAlertCount(activeAlerts)
                .openWorkOrderCount(performance.getOpenWorkOrderCount())
                .maintenanceCompletionRate(performance.getMaintenanceCompletionRate())
                .energyTrend(calculationService.groupReadings(readings, trendPeriod))
                .build();
    }

    @Override
    public StatisticsResponse getStatistics(
            UUID facilityId,
            UUID assetId,
            LocalDateTime from,
            LocalDateTime to,
            AggregationPeriod groupBy) {
        validateQuery(facilityId, assetId, from, to);
        List<SensorReadingData> readings = externalDataService.getReadings(facilityId, assetId, from, to);
        List<BigDecimal> values = readings.stream()
                .filter(reading -> reading.getValue() != null)
                .map(calculationService::normalizeReading)
                .toList();
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal average = values.isEmpty()
                ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(values.size()), 4, RoundingMode.HALF_UP);
        BigDecimal min = values.stream().min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        BigDecimal max = values.stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        List<TimeSeriesPoint> series = calculationService.groupReadings(readings, groupBy);

        return StatisticsResponse.builder()
                .facilityId(facilityId)
                .assetId(assetId)
                .periodStart(from)
                .periodEnd(to)
                .groupedBy(groupBy)
                .totalEnergy(total.setScale(4, RoundingMode.HALF_UP))
                .averageEnergy(average.setScale(4, RoundingMode.HALF_UP))
                .minimumEnergy(min.setScale(4, RoundingMode.HALF_UP))
                .maximumEnergy(max.setScale(4, RoundingMode.HALF_UP))
                .readingCount(values.size())
                .series(series)
                .build();
    }

    @Override
    public ReportCollectionResponse getReports() {
        List<ReportSummaryResponse> reports = new ArrayList<>();
        energyReportRepository.findAll().stream().map(this::energySummary).forEach(reports::add);
        performanceReportRepository.findAll().stream().map(this::performanceSummary).forEach(reports::add);
        reports.sort(Comparator.comparing(ReportSummaryResponse::getGeneratedAt).reversed());
        return ReportCollectionResponse.builder()
                .totalReports(reports.size())
                .reports(reports)
                .build();
    }

    private void validateQuery(UUID facilityId, UUID assetId, LocalDateTime from, LocalDateTime to) {
        validationService.validatePeriod(from, to);
        validationService.validateEnergyScope(facilityId, assetId);
    }

    private boolean isActiveAlert(AlertData alert) {
        if (alert.getStatus() == null) {
            return true;
        }
        String status = alert.getStatus().toUpperCase(Locale.ROOT);
        return !status.equals("RESOLVED") && !status.equals("CLOSED") && !status.equals("CLEARED");
    }

    private ReportSummaryResponse energySummary(EnergyReport report) {
        return ReportSummaryResponse.builder()
                .id(report.getId())
                .reportType("ENERGY")
                .name(report.getName())
                .scopeId(report.getAssetId() != null ? report.getAssetId() : report.getFacilityId())
                .periodStart(report.getPeriodStart())
                .periodEnd(report.getPeriodEnd())
                .generatedAt(report.getGeneratedAt())
                .status(report.getReportStatus())
                .build();
    }

    private ReportSummaryResponse performanceSummary(PerformanceReport report) {
        return ReportSummaryResponse.builder()
                .id(report.getId())
                .reportType("PERFORMANCE")
                .name(report.getName())
                .scopeId(report.getScopeId())
                .periodStart(report.getPeriodStart())
                .periodEnd(report.getPeriodEnd())
                .generatedAt(report.getGeneratedAt())
                .status(report.getReportStatus())
                .build();
    }
}

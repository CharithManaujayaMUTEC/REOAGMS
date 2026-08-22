package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.common.enums.ReportStatus;
import com.reoagms.analytics_service.common.enums.ScopeType;
import com.reoagms.analytics_service.common.exception.ResourceNotFoundException;
import com.reoagms.analytics_service.dto.AlertData;
import com.reoagms.analytics_service.dto.AssetData;
import com.reoagms.analytics_service.dto.EnergyMetrics;
import com.reoagms.analytics_service.dto.PerformanceMetrics;
import com.reoagms.analytics_service.dto.PerformanceReportRequest;
import com.reoagms.analytics_service.dto.PerformanceReportResponse;
import com.reoagms.analytics_service.dto.SensorReadingData;
import com.reoagms.analytics_service.dto.WorkOrderData;
import com.reoagms.analytics_service.mapper.PerformanceReportMapper;
import com.reoagms.analytics_service.model.PerformanceReport;
import com.reoagms.analytics_service.repository.PerformanceReportRepository;
import com.reoagms.analytics_service.util.CsvExportUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceReportServiceImpl implements PerformanceReportService {

    private final PerformanceReportRepository repository;
    private final PerformanceReportMapper mapper;
    private final ExternalDataService externalDataService;
    private final AnalyticsCalculationService calculationService;
    private final AnalyticsValidationService validationService;

    @Override
    @Transactional
    public PerformanceReportResponse create(PerformanceReportRequest request) {
        PerformanceReport report = new PerformanceReport();
        generate(report, request);
        return mapper.toResponse(repository.save(report));
    }

    @Override
    public List<PerformanceReportResponse> getAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public PerformanceReportResponse getById(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional
    public PerformanceReportResponse update(UUID id, PerformanceReportRequest request) {
        PerformanceReport report = find(id);
        generate(report, request);
        return mapper.toResponse(repository.save(report));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.delete(find(id));
    }

    @Override
    public byte[] exportCsv(UUID id) {
        PerformanceReport report = find(id);
        return CsvExportUtil.singleRecord(
                List.of("id", "name", "scopeType", "scopeId", "periodStart", "periodEnd",
                        "availabilityPercentage", "efficiencyPercentage", "capacityFactorPercentage",
                        "utilizationPercentage", "downtimeHours", "alertCount", "maintenanceCount",
                        "generatedAt", "status"),
                List.of(report.getId(), report.getName(), report.getScopeType(), safe(report.getScopeId()),
                        report.getPeriodStart(), report.getPeriodEnd(), report.getAvailabilityPercentage(),
                        report.getEfficiencyPercentage(), report.getCapacityFactorPercentage(),
                        report.getUtilizationPercentage(), report.getDowntimeHours(), report.getAlertCount(),
                        report.getMaintenanceCount(), report.getGeneratedAt(), report.getReportStatus()));
    }

    private void generate(PerformanceReport report, PerformanceReportRequest request) {
        validationService.validatePeriod(request.getPeriodStart(), request.getPeriodEnd());
        validationService.validateScope(request.getScopeType(), request.getScopeId());

        UUID facilityId = request.getScopeType() == ScopeType.FACILITY ? request.getScopeId() : null;
        UUID assetId = request.getScopeType() == ScopeType.ASSET ? request.getScopeId() : null;

        List<AssetData> assets = externalDataService.getAssets(facilityId, assetId);
        List<SensorReadingData> readings = externalDataService.getReadings(
                facilityId, assetId, request.getPeriodStart(), request.getPeriodEnd());
        List<AlertData> alerts = externalDataService.getAlerts(
                facilityId, assetId, request.getPeriodStart(), request.getPeriodEnd());
        List<WorkOrderData> workOrders = externalDataService.getWorkOrders(
                facilityId, assetId, request.getPeriodStart(), request.getPeriodEnd());

        EnergyMetrics energy = calculationService.calculateEnergy(
                readings, assets, request.getPeriodStart(), request.getPeriodEnd(), null);
        PerformanceMetrics metrics = calculationService.calculatePerformance(
                energy, assets, alerts, workOrders, request.getPeriodStart(), request.getPeriodEnd());

        report.setName(request.getName().trim());
        report.setScopeType(request.getScopeType());
        report.setScopeId(request.getScopeId());
        report.setPeriodStart(request.getPeriodStart());
        report.setPeriodEnd(request.getPeriodEnd());
        report.setAvailabilityPercentage(metrics.getAvailabilityPercentage());
        report.setEfficiencyPercentage(metrics.getEfficiencyPercentage());
        report.setCapacityFactorPercentage(metrics.getCapacityFactorPercentage());
        report.setUtilizationPercentage(metrics.getUtilizationPercentage());
        report.setDowntimeHours(metrics.getDowntimeHours());
        report.setAlertCount(metrics.getAlertCount());
        report.setMaintenanceCount(metrics.getMaintenanceCount());
        report.setGeneratedAt(LocalDateTime.now());
        report.setReportStatus(ReportStatus.GENERATED);
    }

    private PerformanceReport find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Performance report not found: " + id));
    }

    private Object safe(Object value) {
        return value == null ? "" : value;
    }
}

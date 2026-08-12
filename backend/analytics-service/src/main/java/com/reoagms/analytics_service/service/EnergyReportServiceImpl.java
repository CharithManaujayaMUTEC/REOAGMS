package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.common.enums.ReportStatus;
import com.reoagms.analytics_service.common.exception.ResourceNotFoundException;
import com.reoagms.analytics_service.dto.AssetData;
import com.reoagms.analytics_service.dto.EnergyMetrics;
import com.reoagms.analytics_service.dto.EnergyReportRequest;
import com.reoagms.analytics_service.dto.EnergyReportResponse;
import com.reoagms.analytics_service.dto.SensorReadingData;
import com.reoagms.analytics_service.mapper.EnergyReportMapper;
import com.reoagms.analytics_service.model.EnergyReport;
import com.reoagms.analytics_service.repository.EnergyReportRepository;
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
public class EnergyReportServiceImpl implements EnergyReportService {

    private final EnergyReportRepository repository;
    private final EnergyReportMapper mapper;
    private final ExternalDataService externalDataService;
    private final AnalyticsCalculationService calculationService;
    private final AnalyticsValidationService validationService;

    @Override
    @Transactional
    public EnergyReportResponse create(EnergyReportRequest request) {
        EnergyReport report = new EnergyReport();
        generate(report, request);
        return mapper.toResponse(repository.save(report));
    }

    @Override
    public List<EnergyReportResponse> getAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public EnergyReportResponse getById(UUID id) {
        return mapper.toResponse(find(id));
    }

    @Override
    @Transactional
    public EnergyReportResponse update(UUID id, EnergyReportRequest request) {
        EnergyReport report = find(id);
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
        EnergyReport report = find(id);
        return CsvExportUtil.singleRecord(
                List.of("id", "name", "facilityId", "assetId", "periodStart", "periodEnd",
                        "totalEnergy", "averageEnergy", "peakEnergy", "expectedEnergy",
                        "variancePercentage", "unit", "generatedAt", "status"),
                List.of(report.getId(), report.getName(), safe(report.getFacilityId()), safe(report.getAssetId()),
                        report.getPeriodStart(), report.getPeriodEnd(), report.getTotalEnergy(),
                        report.getAverageEnergy(), report.getPeakEnergy(), report.getExpectedEnergy(),
                        report.getVariancePercentage(), report.getUnit(), report.getGeneratedAt(),
                        report.getReportStatus()));
    }

    private void generate(EnergyReport report, EnergyReportRequest request) {
        validationService.validatePeriod(request.getPeriodStart(), request.getPeriodEnd());
        validationService.validateEnergyScope(request.getFacilityId(), request.getAssetId());

        List<AssetData> assets = externalDataService.getAssets(request.getFacilityId(), request.getAssetId());
        List<SensorReadingData> readings = externalDataService.getReadings(
                request.getFacilityId(), request.getAssetId(), request.getPeriodStart(), request.getPeriodEnd());
        EnergyMetrics metrics = calculationService.calculateEnergy(
                readings, assets, request.getPeriodStart(), request.getPeriodEnd(), request.getExpectedEnergy());

        report.setName(request.getName().trim());
        report.setFacilityId(request.getFacilityId());
        report.setAssetId(request.getAssetId());
        report.setPeriodStart(request.getPeriodStart());
        report.setPeriodEnd(request.getPeriodEnd());
        report.setTotalEnergy(metrics.getTotalEnergy());
        report.setAverageEnergy(metrics.getAverageEnergy());
        report.setPeakEnergy(metrics.getPeakEnergy());
        report.setExpectedEnergy(metrics.getExpectedEnergy());
        report.setVariancePercentage(metrics.getVariancePercentage());
        report.setUnit(metrics.getUnit());
        report.setGeneratedAt(LocalDateTime.now());
        report.setReportStatus(ReportStatus.GENERATED);
    }

    private EnergyReport find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Energy report not found: " + id));
    }

    private Object safe(Object value) {
        return value == null ? "" : value;
    }
}

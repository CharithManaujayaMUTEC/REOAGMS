package com.reoagms.analytics_service.service;

import com.reoagms.analytics_service.dto.AssetData;
import com.reoagms.analytics_service.dto.EnergyReportRequest;
import com.reoagms.analytics_service.dto.EnergyReportResponse;
import com.reoagms.analytics_service.dto.SensorReadingData;
import com.reoagms.analytics_service.mapper.EnergyReportMapper;
import com.reoagms.analytics_service.model.EnergyReport;
import com.reoagms.analytics_service.repository.EnergyReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnergyReportServiceImplTest {

    @Mock
    private EnergyReportRepository repository;

    @Mock
    private ExternalDataService externalDataService;

    private EnergyReportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EnergyReportServiceImpl(
                repository,
                new EnergyReportMapper(),
                externalDataService,
                new AnalyticsCalculationService(),
                new AnalyticsValidationService());
    }

    @Test
    void generatesAndPersistsEnergyReportFromExternalData() {
        UUID facilityId = UUID.randomUUID();
        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = from.plusHours(2);
        EnergyReportRequest request = new EnergyReportRequest();
        request.setName("Daily generation");
        request.setFacilityId(facilityId);
        request.setPeriodStart(from);
        request.setPeriodEnd(to);

        AssetData asset = new AssetData();
        asset.setRatedCapacity(new BigDecimal("100"));
        asset.setStatus("ACTIVE");
        SensorReadingData reading = new SensorReadingData();
        reading.setTimestamp(from.plusHours(1));
        reading.setValue(new BigDecimal("150"));
        reading.setUnit("kWh");

        when(externalDataService.getAssets(facilityId, null)).thenReturn(List.of(asset));
        when(externalDataService.getReadings(facilityId, null, from, to)).thenReturn(List.of(reading));
        when(repository.save(any(EnergyReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EnergyReportResponse response = service.create(request);

        assertThat(response.getName()).isEqualTo("Daily generation");
        assertThat(response.getTotalEnergy()).isEqualByComparingTo("150.0000");
        assertThat(response.getExpectedEnergy()).isEqualByComparingTo("200.0000");
        assertThat(response.getVariancePercentage()).isEqualByComparingTo("-25.0000");
    }
}

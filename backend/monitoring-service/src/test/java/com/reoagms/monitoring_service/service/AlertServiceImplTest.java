package com.reoagms.monitoring_service.service;

import com.reoagms.monitoring_service.common.enums.AlertSeverity;
import com.reoagms.monitoring_service.common.enums.AlertStatus;
import com.reoagms.monitoring_service.common.enums.ConditionOperator;
import com.reoagms.monitoring_service.common.enums.MetricType;
import com.reoagms.monitoring_service.dto.AlertResponse;
import com.reoagms.monitoring_service.mapper.AlertMapper;
import com.reoagms.monitoring_service.model.Alert;
import com.reoagms.monitoring_service.model.AlertRule;
import com.reoagms.monitoring_service.model.SensorReading;
import com.reoagms.monitoring_service.repository.AlertRepository;
import com.reoagms.monitoring_service.repository.AlertRuleRepository;
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
class AlertServiceImplTest {

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private AlertRuleRepository alertRuleRepository;

    private AlertServiceImpl alertService;

    private final AlertMapper mapper = new AlertMapper();

    @BeforeEach
    void setUp() {
        alertService = new AlertServiceImpl(alertRepository, alertRuleRepository, mapper);
    }

    @Test
    void raisesAlertWhenReadingBreachesAssetSpecificRule() {
        UUID assetId = UUID.randomUUID();
        SensorReading reading = SensorReading.builder()
                .sensorId(UUID.randomUUID())
                .assetId(assetId)
                .facilityId(UUID.randomUUID())
                .timestamp(LocalDateTime.now())
                .value(new BigDecimal("95"))
                .unit("C")
                .metricType(MetricType.TEMPERATURE)
                .build();

        AlertRule rule = AlertRule.builder()
                .name("High temperature")
                .assetId(assetId)
                .metricType(MetricType.TEMPERATURE)
                .operator(ConditionOperator.GREATER_THAN)
                .threshold(new BigDecimal("80"))
                .severity(AlertSeverity.HIGH)
                .enabled(true)
                .build();

        when(alertRuleRepository.findByEnabledTrueAndMetricTypeAndAssetId(MetricType.TEMPERATURE, assetId))
                .thenReturn(List.of(rule));
        when(alertRuleRepository.findByEnabledTrueAndMetricTypeAndAssetIdIsNull(MetricType.TEMPERATURE))
                .thenReturn(List.of());
        when(alertRepository.save(any(Alert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<AlertResponse> generated = alertService.evaluateReading(reading);

        assertThat(generated).hasSize(1);
        AlertResponse alert = generated.get(0);
        assertThat(alert.getSeverity()).isEqualTo(AlertSeverity.HIGH);
        assertThat(alert.getStatus()).isEqualTo(AlertStatus.OPEN);
        assertThat(alert.getAssetId()).isEqualTo(assetId);
        assertThat(alert.getThreshold()).isEqualByComparingTo("80");
    }

    @Test
    void doesNotRaiseAlertWhenReadingIsWithinThreshold() {
        UUID assetId = UUID.randomUUID();
        SensorReading reading = SensorReading.builder()
                .sensorId(UUID.randomUUID())
                .assetId(assetId)
                .timestamp(LocalDateTime.now())
                .value(new BigDecimal("50"))
                .metricType(MetricType.TEMPERATURE)
                .build();

        AlertRule rule = AlertRule.builder()
                .name("High temperature")
                .assetId(assetId)
                .metricType(MetricType.TEMPERATURE)
                .operator(ConditionOperator.GREATER_THAN)
                .threshold(new BigDecimal("80"))
                .severity(AlertSeverity.HIGH)
                .enabled(true)
                .build();

        when(alertRuleRepository.findByEnabledTrueAndMetricTypeAndAssetId(MetricType.TEMPERATURE, assetId))
                .thenReturn(List.of(rule));
        when(alertRuleRepository.findByEnabledTrueAndMetricTypeAndAssetIdIsNull(MetricType.TEMPERATURE))
                .thenReturn(List.of());

        List<AlertResponse> generated = alertService.evaluateReading(reading);

        assertThat(generated).isEmpty();
    }
}

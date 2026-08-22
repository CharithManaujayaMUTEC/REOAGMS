package com.reoagms.monitoring_service.controller;

import com.reoagms.monitoring_service.common.enums.MetricType;
import com.reoagms.monitoring_service.common.exception.GlobalExceptionHandler;
import com.reoagms.monitoring_service.common.exception.ResourceNotFoundException;
import com.reoagms.monitoring_service.dto.SensorReadingRequest;
import com.reoagms.monitoring_service.dto.SensorReadingResponse;
import com.reoagms.monitoring_service.service.SensorReadingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SensorReadingControllerTest {

    @Mock
    private SensorReadingService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new SensorReadingController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReturnsCreatedReadingAsPlainJson() throws Exception {
        UUID sensorId = UUID.randomUUID();
        UUID assetId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.of(2026, 8, 14, 12, 0, 0);

        SensorReadingResponse response = SensorReadingResponse.builder()
                .id(UUID.randomUUID())
                .sensorId(sensorId)
                .assetId(assetId)
                .timestamp(timestamp)
                .value(new BigDecimal("24.5"))
                .unit("C")
                .metricType(MetricType.TEMPERATURE)
                .build();

        when(service.create(any(SensorReadingRequest.class))).thenReturn(response);

        String requestBody = """
                {
                  "sensorId": "%s",
                  "assetId": "%s",
                  "timestamp": "%s",
                  "value": 24.5,
                  "unit": "C",
                  "metricType": "TEMPERATURE"
                }
                """.formatted(sensorId, assetId, timestamp);

        mockMvc.perform(post("/api/v1/sensor-readings")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.value").value(24.5))
                .andExpect(jsonPath("$.unit").value("C"));
    }

    @Test
    void getAllReturnsPlainArrayForCrossServiceConsumers() throws Exception {
        when(service.getAll(null, null, null, null, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/sensor-readings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getByIdReturns404WhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenThrow(new ResourceNotFoundException("Sensor reading not found: " + id));

        mockMvc.perform(get("/api/v1/sensor-readings/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}

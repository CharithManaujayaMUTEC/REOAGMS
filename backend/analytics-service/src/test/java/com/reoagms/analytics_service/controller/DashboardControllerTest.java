package com.reoagms.analytics_service.controller;

import com.reoagms.analytics_service.common.exception.GlobalExceptionHandler;
import com.reoagms.analytics_service.common.exception.ResourceNotFoundException;
import com.reoagms.analytics_service.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new DashboardController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsDashboardListInStandardEnvelope() throws Exception {
        when(service.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/dashboards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Dashboards retrieved"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void mapsMissingDashboardToNotFoundResponse() throws Exception {
        UUID id = UUID.randomUUID();
        when(service.getById(id)).thenThrow(new ResourceNotFoundException("Dashboard not found: " + id));

        mockMvc.perform(get("/api/v1/dashboards/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Dashboard not found: " + id));
    }
}

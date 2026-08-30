package com.reoagms.notification_service.notification.controller;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.common.enums.NotificationStatus;
import com.reoagms.notification_service.common.exception.GlobalExceptionHandler;
import com.reoagms.notification_service.common.exception.ResourceNotFoundException;
import com.reoagms.notification_service.notification.dto.NotificationRequest;
import com.reoagms.notification_service.notification.dto.NotificationResponse;
import com.reoagms.notification_service.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new NotificationController(notificationService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createReturnsCreatedNotification() throws Exception {
        String requestBody = """
                {
                  "recipient": "ops@reoagms.dev",
                  "channel": "EMAIL",
                  "subject": "Alert",
                  "message": "Threshold exceeded"
                }
                """;

        NotificationResponse response = NotificationResponse.builder()
                .id(UUID.randomUUID())
                .recipient("ops@reoagms.dev")
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.SENT)
                .message("Threshold exceeded")
                .subject("Alert")
                .retryCount(0)
                .build();

        when(notificationService.create(any(NotificationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.recipient").value("ops@reoagms.dev"));
    }

    @Test
    void createRejectsMissingRecipient() throws Exception {
        String requestBody = """
                {
                  "channel": "EMAIL",
                  "message": "Threshold exceeded"
                }
                """;

        mockMvc.perform(post("/api/v1/notifications")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.recipient").exists());
    }

    @Test
    void getAllReturnsList() throws Exception {
        when(notificationService.getAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getByIdMapsMissingNotificationTo404() throws Exception {
        UUID id = UUID.randomUUID();
        when(notificationService.getById(id)).thenThrow(new ResourceNotFoundException("Notification not found"));

        mockMvc.perform(get("/api/v1/notifications/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Notification not found"));
    }
}

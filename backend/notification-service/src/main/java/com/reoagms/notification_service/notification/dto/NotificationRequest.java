package com.reoagms.notification_service.notification.dto;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NotificationRequest {

    @NotBlank
    private String recipient;

    @NotNull
    private NotificationChannel channel;

    @NotBlank
    private String message;

    private String subject;

    private String relatedEntityId;

}

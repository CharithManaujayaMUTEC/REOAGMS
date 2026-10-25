package com.reoagms.notification_service.notification.dto;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.common.enums.NotificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {

    private UUID id;

    private String recipient;

    private NotificationChannel channel;

    private NotificationStatus status;

    private String message;

    private String subject;

    private String relatedEntityId;

    private int retryCount;

    private String failureReason;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}

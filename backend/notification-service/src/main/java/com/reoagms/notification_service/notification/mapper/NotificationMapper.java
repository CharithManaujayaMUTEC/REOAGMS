package com.reoagms.notification_service.notification.mapper;

import com.reoagms.notification_service.notification.dto.NotificationRequest;
import com.reoagms.notification_service.notification.dto.NotificationResponse;
import com.reoagms.notification_service.notification.model.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationRequest request) {

        return Notification.builder()
                .recipient(request.getRecipient())
                .channel(request.getChannel())
                .subject(request.getSubject())
                .message(request.getMessage())
                .sourceService(request.getSourceService())
                .referenceId(request.getReferenceId())
                .build();

    }

    public NotificationResponse toResponse(Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .recipient(notification.getRecipient())
                .channel(notification.getChannel())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .status(notification.getStatus())
                .retryCount(notification.getRetryCount())
                .failureReason(notification.getFailureReason())
                .sourceService(notification.getSourceService())
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();

    }

}

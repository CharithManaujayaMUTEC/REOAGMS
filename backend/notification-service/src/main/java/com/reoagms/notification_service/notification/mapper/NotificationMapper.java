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
                .message(request.getMessage())
                .subject(request.getSubject())
                .relatedEntityId(request.getRelatedEntityId())
                .build();

    }

    public NotificationResponse toResponse(Notification notification) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .recipient(notification.getRecipient())
                .channel(notification.getChannel())
                .status(notification.getStatus())
                .message(notification.getMessage())
                .subject(notification.getSubject())
                .relatedEntityId(notification.getRelatedEntityId())
                .retryCount(notification.getRetryCount())
                .failureReason(notification.getFailureReason())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();

    }

}

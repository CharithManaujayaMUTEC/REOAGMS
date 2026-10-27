package com.reoagms.notification_service.notification.model;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.common.enums.NotificationStatus;
import com.reoagms.notification_service.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private NotificationStatus status = NotificationStatus.PENDING;

    @Builder.Default
    @Column(nullable = false)
    private Integer retryCount = 0;

    private String failureReason;

    // Which upstream service triggered this (e.g. "monitoring-service") and its reference id (e.g. alertId)
    private String sourceService;

    private String referenceId;

}

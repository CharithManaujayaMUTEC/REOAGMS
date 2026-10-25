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

    @Column(nullable = false, length = 255)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private NotificationStatus status = NotificationStatus.PENDING;

    @Column(nullable = false, length = 2000)
    private String message;

    private String subject;

    private String relatedEntityId;

    @Builder.Default
    private int retryCount = 0;

    private String failureReason;

}

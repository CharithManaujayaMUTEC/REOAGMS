package com.reoagms.notification_service.notification.service;

import com.reoagms.notification_service.common.enums.NotificationStatus;
import com.reoagms.notification_service.notification.model.Notification;
import com.reoagms.notification_service.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private final NotificationRepository repository;
    private final NotificationService notificationService;

    @Value("${notification.retry.max-attempts:3}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${notification.retry.fixed-delay-ms:60000}")
    public void retryFailedNotifications() {

        for (Notification notification : repository.findByStatus(NotificationStatus.FAILED)) {

            if (notification.getRetryCount() >= maxAttempts) {
                continue;
            }

            notification.setStatus(NotificationStatus.RETRYING);
            repository.save(notification);

            notificationService.retry(notification.getId());

        }

    }

}

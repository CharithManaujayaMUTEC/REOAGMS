package com.reoagms.notification_service.notification.service;

import com.reoagms.notification_service.common.enums.NotificationStatus;
import com.reoagms.notification_service.notification.model.Notification;
import com.reoagms.notification_service.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRetryScheduler {

    private final NotificationRepository repository;
    private final NotificationServiceImpl notificationService;

    @Value("${notification.retry.max-attempts:3}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${notification.retry.fixed-delay-ms:60000}")
    public void retryFailedNotifications() {

        List<Notification> failed = repository.findByStatusAndRetryCountLessThan(
                NotificationStatus.FAILED, maxAttempts);

        if (failed.isEmpty()) {
            return;
        }

        log.info("Retrying {} failed notification(s)", failed.size());

        failed.forEach(notification -> notificationService.retry(notification.getId()));

    }

}

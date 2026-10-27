package com.reoagms.notification_service.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Placeholder push notification integration (e.g. Firebase Cloud Messaging).
 */
@Slf4j
@Service
public class PushSenderService {

    public void send(String recipient, String subject, String message) {

        // TODO: integrate FCM/APNS
        log.info("Push sent to {}: {} - {}", recipient, subject, message);

    }

}

package com.reoagms.notification_service.sms.service;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.notification.model.Notification;
import com.reoagms.notification_service.notification.service.NotificationDispatcher;
import com.reoagms.notification_service.sms.model.SmsNotification;
import com.reoagms.notification_service.sms.repository.SmsNotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Stub dispatcher: logs and records the SMS locally instead of calling a real
 * provider (e.g. Twilio). Swap the body of dispatch() for a provider SDK call
 * once credentials are available.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SmsDispatcher implements NotificationDispatcher {

    private final SmsNotificationRepository repository;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void dispatch(Notification notification) {

        log.info("Sending SMS to {}: {}", notification.getRecipient(), notification.getMessage());

        SmsNotification sms = SmsNotification.builder()
                .phoneNumber(notification.getRecipient())
                .message(notification.getMessage())
                .providerResponse("SIMULATED_OK")
                .build();

        repository.save(sms);

    }

}

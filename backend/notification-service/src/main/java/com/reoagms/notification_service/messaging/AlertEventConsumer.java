package com.reoagms.notification_service.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.notification.dto.NotificationRequest;
import com.reoagms.notification_service.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEventConsumer {
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    @Value("${reoagms.alert-recipient:operations@example.com}") private String recipient;

    @RabbitListener(queues = "notification.alert-opened.v1")
    public void onAlertOpened(String payload) throws Exception {
        AlertEvent event = objectMapper.readValue(payload, AlertEvent.class);
        if (event.schemaVersion() != 1 || event.alertId() == null) {
            throw new IllegalArgumentException("Unsupported or invalid alert event");
        }
        NotificationRequest request = new NotificationRequest();
        request.setRecipient(recipient);
        request.setChannel(NotificationChannel.EMAIL);
        request.setSubject("REOAGMS alert: " + (event.severity() == null ? "UNKNOWN" : event.severity()));
        request.setMessage(event.message() + " | alertId=" + event.alertId() + " | assetId=" + event.assetId());
        request.setRelatedEntityId(event.alertId().toString());
        notificationService.create(request);
        log.info("Processed alert event {} for alert {}", event.eventId(), event.alertId());
    }
}

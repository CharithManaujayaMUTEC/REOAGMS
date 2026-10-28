package com.reoagms.notification_service.messaging;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import tools.jackson.databind.ObjectMapper;
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
    private final SimpMessagingTemplate messagingTemplate;
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

        // Same alert, delivered in-app so operators are notified even when SMTP is not configured.
        NotificationRequest inApp = new NotificationRequest();
        inApp.setRecipient("operations-console");
        inApp.setChannel(NotificationChannel.PUSH);
        inApp.setSubject(request.getSubject());
        inApp.setMessage(request.getMessage());
        inApp.setRelatedEntityId(request.getRelatedEntityId());
        notificationService.create(inApp);
        messagingTemplate.convertAndSend("/topic/alerts", event); // FR-14: live dashboard push
        log.info("Processed alert event {} for alert {}", event.eventId(), event.alertId());
    }
}

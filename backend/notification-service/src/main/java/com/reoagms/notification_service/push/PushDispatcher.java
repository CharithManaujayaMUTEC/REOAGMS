package com.reoagms.notification_service.push;

import com.reoagms.notification_service.common.enums.NotificationChannel;
import com.reoagms.notification_service.notification.model.Notification;
import com.reoagms.notification_service.notification.service.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/** In-app delivery: pushes the notification to every connected console over STOMP (no SMTP needed). */
@Component
@RequiredArgsConstructor
public class PushDispatcher implements NotificationDispatcher {
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public void dispatch(Notification notification) {
        messagingTemplate.convertAndSend("/topic/notifications", (Object) Map.of(
                "subject", String.valueOf(notification.getSubject()),
                "message", String.valueOf(notification.getMessage()),
                "relatedEntityId", String.valueOf(notification.getRelatedEntityId())));
    }
}

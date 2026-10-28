package com.reoagms.monitoring_service.messaging;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.reoagms.monitoring_service.dto.AlertResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public void publish(AlertResponse alert) {
        AlertEvent event = new AlertEvent(1, UUID.randomUUID(), alert.getId(), alert.getFacilityId(),
                alert.getAssetId(), alert.getMetricType() == null ? null : alert.getMetricType().name(),
                alert.getValue(), alert.getThreshold(), alert.getSeverity() == null ? null : alert.getSeverity().name(),
                alert.getMessage(), alert.getTimestamp());
        try {
            rabbitTemplate.convertAndSend(RabbitMessagingConfig.EXCHANGE, RabbitMessagingConfig.ALERT_ROUTING_KEY,
                    objectMapper.writeValueAsString(event));
        } catch (JacksonException ex) {
            // Do not silently lose the reason when an event cannot be serialized.
            throw new IllegalStateException("Could not serialize monitoring alert event", ex);
        }
    }
}

package com.reoagms.maintenance_service.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MaintenanceMessagingConfig {
    public static final String QUEUE = "maintenance.alert-opened.v1";

    @Bean TopicExchange reoagmsEventsExchange() { return new TopicExchange("reoagms.events", true, false); }
    @Bean Queue maintenanceAlertQueue() { return new Queue(QUEUE, true); }
    @Bean Binding maintenanceAlertBinding(Queue maintenanceAlertQueue, TopicExchange reoagmsEventsExchange) {
        return BindingBuilder.bind(maintenanceAlertQueue).to(reoagmsEventsExchange).with("monitoring.alert.opened.v1");
    }
}

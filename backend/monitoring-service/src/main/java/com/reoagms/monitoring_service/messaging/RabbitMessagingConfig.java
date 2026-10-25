package com.reoagms.monitoring_service.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMessagingConfig {
    public static final String EXCHANGE = "reoagms.events";
    public static final String ALERT_ROUTING_KEY = "monitoring.alert.opened.v1";
    @Bean
    TopicExchange reoagmsEventsExchange() { return new TopicExchange(EXCHANGE, true, false); }
}

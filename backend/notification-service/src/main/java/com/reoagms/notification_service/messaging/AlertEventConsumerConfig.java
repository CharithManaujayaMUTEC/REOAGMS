package com.reoagms.notification_service.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class AlertEventConsumerConfig {
    @Bean TopicExchange reoagmsEventsExchange() { return new TopicExchange("reoagms.events", true, false); }
    @Bean DirectExchange reoagmsDeadLetterExchange() { return new DirectExchange("reoagms.events.dlx", true, false); }
    @Bean Queue alertOpenedQueue() {
        return new Queue("notification.alert-opened.v1", true, false, false, Map.of(
                "x-dead-letter-exchange", "reoagms.events.dlx",
                "x-dead-letter-routing-key", "notification.alert-opened.v1.dead"));
    }
    @Bean Queue alertOpenedDeadLetterQueue() { return new Queue("notification.alert-opened.v1.dlq", true); }
    @Bean Binding alertOpenedBinding(Queue alertOpenedQueue, TopicExchange reoagmsEventsExchange) {
        return BindingBuilder.bind(alertOpenedQueue).to(reoagmsEventsExchange).with("monitoring.alert.opened.v1");
    }
    @Bean Binding alertOpenedDeadLetterBinding(Queue alertOpenedDeadLetterQueue, DirectExchange reoagmsDeadLetterExchange) {
        return BindingBuilder.bind(alertOpenedDeadLetterQueue).to(reoagmsDeadLetterExchange).with("notification.alert-opened.v1.dead");
    }
}

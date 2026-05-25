package com.ecommerce.notification.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean public Queue orderNotificationQueue() { return new Queue("order.notification.queue", true); }
    @Bean public Queue paymentNotificationQueue() { return new Queue("payment.notification.queue", true); }
    @Bean public TopicExchange notificationExchange() { return new TopicExchange("notification.exchange"); }
    @Bean public Binding orderBinding(Queue orderNotificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(orderNotificationQueue).to(notificationExchange).with("order.*");
    }
    @Bean public Binding paymentBinding(Queue paymentNotificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(paymentNotificationQueue).to(notificationExchange).with("payment.*");
    }
}

package com.ecommerce.payment.infrastructure.messaging;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * payment-service RabbitMQ 設定
 *
 * <p>payment-service 只需消費 {@code payment.request.queue}
 * 並發布結果到 {@code payment.result.queue}（由 order-service 消費）。
 * Exchange / Queue 宣告集中在 order-service 的 SagaRabbitMQConfig，
 * 此處只設定 JSON converter 與 RabbitTemplate。</p>
 */
@Configuration
public class SagaRabbitMQConfig {

    public static final String SAGA_EXCHANGE       = "saga.exchange";
    public static final String RK_PAYMENT_RESULT   = "payment.result";
    public static final String RK_NOTIFICATION     = "notification";
    public static final String Q_PAYMENT_REQUEST   = "payment.request.queue";

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}

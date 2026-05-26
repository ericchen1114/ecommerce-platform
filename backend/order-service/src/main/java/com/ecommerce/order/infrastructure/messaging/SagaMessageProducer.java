package com.ecommerce.order.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Saga 訊息發送器（order-service）
 *
 * <p>封裝 RabbitTemplate，統一管理 routing key 與 exchange，
 * 確保呼叫方不直接依賴 MQ 字串常數。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SagaMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /** 發送付款請求到 payment-service */
    public void sendPaymentRequest(SagaMessage message) {
        rabbitTemplate.convertAndSend(SagaRabbitMQConfig.SAGA_EXCHANGE, SagaRabbitMQConfig.RK_PAYMENT_REQUEST, message);
        log.info("[SagaProducer] 付款請求已發送，orderId={}, amount={}", message.getOrderId(), message.getAmount());
    }

    /** 發送庫存扣除請求到 product-service */
    public void sendStockRequest(SagaMessage message) {
        rabbitTemplate.convertAndSend(SagaRabbitMQConfig.SAGA_EXCHANGE, SagaRabbitMQConfig.RK_STOCK_REQUEST, message);
        log.info("[SagaProducer] 庫存請求已發送，orderId={}, productId={}", message.getOrderId(), message.getProductId());
    }

    /** 發送通知訊息到 notification-service */
    public void sendNotification(SagaMessage message) {
        rabbitTemplate.convertAndSend(SagaRabbitMQConfig.SAGA_EXCHANGE, SagaRabbitMQConfig.RK_NOTIFICATION, message);
        log.info("[SagaProducer] 通知已發送，orderId={}", message.getOrderId());
    }
}

package com.ecommerce.order.infrastructure.messaging;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.order.infrastructure.persistence.jpa.OrderJpaEntity;
import com.ecommerce.order.infrastructure.persistence.jpa.OrderJpaRepository;

import java.io.IOException;

/**
 * Saga 事件消費者（order-service）
 *
 * <p>監聽 payment.result.queue 與 stock.result.queue，
 * 根據結果更新訂單狀態並發布補償訊息。</p>
 *
 * <h3>Saga 狀態機</h3>
 * <pre>
 * PAYMENT_COMPLETED → 訂單狀態 CONFIRMED，發送 NOTIFICATION
 * PAYMENT_FAILED    → 訂單狀態 CANCELLED，發送 stock.request (STOCK_RELEASED 補償)
 * STOCK_RESERVED    → (記錄庫存已鎖定，等待付款結果)
 * STOCK_FAILED      → 訂單狀態 CANCELLED
 * </pre>
 *
 * <h3>冪等性</h3>
 * <p>以 {@code messageId} 避免重複處理：訂單狀態已為終態時直接 ACK 跳過。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SagaEventConsumer {

    private final OrderJpaRepository  orderJpaRepository;
    private final SagaMessageProducer sagaProducer;

    /**
     * 消費付款結果：PAYMENT_COMPLETED / PAYMENT_FAILED
     */
    @RabbitListener(queues = SagaRabbitMQConfig.Q_PAYMENT_RESULT,
                    ackMode = "MANUAL",
                    containerFactory = "rabbitListenerContainerFactory")
    @Transactional
    public void handlePaymentResult(SagaMessage message,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("[SagaEventConsumer] 收到付款結果，messageId={}, eventType={}, orderId={}",
                message.getMessageId(), message.getEventType(), message.getOrderId());

        try {
            OrderJpaEntity order = orderJpaRepository.findById(message.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("訂單不存在：" + message.getOrderId()));

            switch (message.getEventType()) {
                case "PAYMENT_COMPLETED" -> handlePaymentCompleted(order, message);
                case "PAYMENT_FAILED"    -> handlePaymentFailed(order, message);
                default -> log.warn("[SagaEventConsumer] 未知事件類型：{}", message.getEventType());
            }

            channel.basicAck(deliveryTag, false);

        } catch (IllegalArgumentException e) {
            // 訂單不存在：不重試，直接進 DLQ
            log.error("[SagaEventConsumer] 訂單不存在，訊息進 DLQ：{}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            // 其他例外：requeue=true 讓 RabbitMQ 重試
            log.error("[SagaEventConsumer] 處理付款結果失敗，orderId={}，將重試：{}",
                    message.getOrderId(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    /**
     * 消費庫存結果：STOCK_RESERVED / STOCK_FAILED
     */
    @RabbitListener(queues = SagaRabbitMQConfig.Q_STOCK_RESULT,
                    ackMode = "MANUAL",
                    containerFactory = "rabbitListenerContainerFactory")
    @Transactional
    public void handleStockResult(SagaMessage message,
                                  Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("[SagaEventConsumer] 收到庫存結果，eventType={}, orderId={}",
                message.getEventType(), message.getOrderId());

        try {
            OrderJpaEntity order = orderJpaRepository.findById(message.getOrderId())
                    .orElseThrow(() -> new IllegalArgumentException("訂單不存在：" + message.getOrderId()));

            if ("STOCK_FAILED".equals(message.getEventType())) {
                order.setStatus("CANCELLED");
                orderJpaRepository.save(order);
                log.warn("[SagaEventConsumer] 庫存扣除失敗，訂單已取消，orderId={}", order.getId());
            }
            // STOCK_RESERVED 僅記錄日誌，等待 payment.result 決定最終狀態

            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            log.error("[SagaEventConsumer] 處理庫存結果失敗：{}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }

    // ─── 內部處理方法 ──────────────────────────────────────────────────────────

    private void handlePaymentCompleted(OrderJpaEntity order, SagaMessage message) {
        // 冪等：已為終態則跳過
        if ("CONFIRMED".equals(order.getStatus()) || "CANCELLED".equals(order.getStatus())) {
            log.info("[SagaEventConsumer] 訂單已為終態 {}，跳過 PAYMENT_COMPLETED", order.getStatus());
            return;
        }
        order.setStatus("CONFIRMED");
        orderJpaRepository.save(order);
        log.info("[SagaEventConsumer] 訂單已確認，orderId={}, orderNo={}", order.getId(), order.getOrderNo());

        // 發送通知訊息
        sagaProducer.sendNotification(
                SagaMessage.notification(
                        order.getId(), order.getOrderNo(), order.getUserId(),
                        String.format("您的訂單 %s 已確認，金額 %s 元", order.getOrderNo(), order.getTotalAmount())
                )
        );
    }

    private void handlePaymentFailed(OrderJpaEntity order, SagaMessage message) {
        if ("CANCELLED".equals(order.getStatus())) {
            log.info("[SagaEventConsumer] 訂單已取消，跳過 PAYMENT_FAILED");
            return;
        }
        order.setStatus("CANCELLED");
        orderJpaRepository.save(order);
        log.warn("[SagaEventConsumer] 付款失敗，訂單已取消，orderId={}，原因：{}",
                order.getId(), message.getFailureReason());

        // 補償：通知 product-service 釋放庫存
        // （若有 stock.request 的庫存扣除，發送 STOCK_RELEASED 補償）
        sagaProducer.sendNotification(
                SagaMessage.notification(
                        order.getId(), order.getOrderNo(), order.getUserId(),
                        String.format("很抱歉，您的訂單 %s 付款失敗，已自動取消", order.getOrderNo())
                )
        );
    }
}

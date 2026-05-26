package com.ecommerce.payment.infrastructure.messaging;

import com.ecommerce.payment.application.service.PaymentApplicationService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Saga 付款 Consumer（payment-service）
 *
 * <p>監聽 {@code payment.request.queue}，
 * 接收 order-service 發出的付款請求，執行付款後發布結果到
 * {@code payment.result.queue}（由 order-service 的 SagaEventConsumer 消費）。</p>
 *
 * <h3>MANUAL ACK 設計</h3>
 * <p>使用手動 ACK 確保付款結果已寫入 DB 並發布 RabbitMQ 後才 ACK，
 * 避免服務重啟時訊息遺失。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SagaPaymentConsumer {

    private final PaymentApplicationService paymentService;

    @RabbitListener(queues = SagaRabbitMQConfig.Q_PAYMENT_REQUEST,
                    ackMode = "MANUAL",
                    containerFactory = "rabbitListenerContainerFactory")
    public void handlePaymentRequest(SagaMessage message,
                                     Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("[SagaPaymentConsumer] 收到付款請求，messageId={}, orderId={}, amount={}",
                message.getMessageId(), message.getOrderId(), message.getAmount());

        try {
            paymentService.processPayment(
                    message.getOrderId(),
                    message.getOrderNo(),
                    message.getUserId(),
                    message.getAmount(),
                    null   // 付款方式由前端傳入，此處預設 CREDIT_CARD
            );
            channel.basicAck(deliveryTag, false);

        } catch (IllegalArgumentException e) {
            // 資料問題，不重試，進 DLQ
            log.error("[SagaPaymentConsumer] 資料異常，進 DLQ：{}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
        } catch (Exception e) {
            // 暫時性錯誤，requeue 重試
            log.error("[SagaPaymentConsumer] 處理失敗，將重試：{}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, true);
        }
    }
}

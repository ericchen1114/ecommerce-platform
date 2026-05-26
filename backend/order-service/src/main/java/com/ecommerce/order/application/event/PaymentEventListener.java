package com.ecommerce.order.application.event;

import com.ecommerce.order.domain.event.OrderCreatedEvent;
import com.ecommerce.order.domain.event.OrderEvent;
import com.ecommerce.order.domain.event.OrderEventListener;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.infrastructure.messaging.SagaMessage;
import com.ecommerce.order.infrastructure.messaging.SagaMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 付款事件監聽器（Observer Pattern - Concrete Observer）
 *
 * <p>監聽 {@link OrderCreatedEvent}，在訂單建立後透過 {@link SagaMessageProducer}
 * 發送 Saga 付款請求到 {@code payment.request.queue}，
 * 由 payment-service Consumer 接收後建立付款記錄並執行扣款。</p>
 *
 * <h3>Saga 流程起點</h3>
 * <pre>
 * OrderApplicationService.createOrder()
 *   → OrderEventPublisher.publish(OrderCreatedEvent)
 *   → PaymentEventListener.onOrderEvent()           ← 此處
 *   → SagaMessageProducer.sendPaymentRequest()
 *   → RabbitMQ payment.request.queue
 *   → payment-service SagaPaymentConsumer
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener implements OrderEventListener {

    private final SagaMessageProducer sagaProducer;

    @Override
    public void onOrderEvent(OrderEvent event) {
        if (!(event instanceof OrderCreatedEvent e)) return;

        Order order = e.getOrder();
        log.info("[PaymentEventListener] 收到訂單建立事件，orderId={}, amount={}",
                order.getId(), order.getTotalAmount());

        try {
            SagaMessage msg = SagaMessage.paymentRequest(
                    order.getId(),
                    order.getOrderNo() != null ? order.getOrderNo() : "",
                    order.getUserId(),
                    order.getTotalAmount()
            );
            sagaProducer.sendPaymentRequest(msg);
        } catch (Exception ex) {
            log.error("[PaymentEventListener] 付款請求發送失敗，orderId={}：{}",
                    order.getId(), ex.getMessage(), ex);
        }
    }

    @Override
    public boolean supports(Class<? extends OrderEvent> eventClass) {
        return OrderCreatedEvent.class.equals(eventClass);
    }
}

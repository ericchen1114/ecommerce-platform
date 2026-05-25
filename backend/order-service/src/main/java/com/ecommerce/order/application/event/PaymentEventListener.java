package com.ecommerce.order.application.event;

import com.ecommerce.order.domain.event.OrderCreatedEvent;
import com.ecommerce.order.domain.event.OrderEvent;
import com.ecommerce.order.domain.event.OrderEventListener;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.infrastructure.client.PaymentFeignClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 付款事件監聽器（Observer Pattern - Concrete Observer）
 *
 * <p>監聽 {@link OrderCreatedEvent}，在訂單建立後透過 RabbitMQ 發送付款請求訊息到
 * payment-service，由 Saga 流程接管後續扣款。</p>
 *
 * <h3>設計說明</h3>
 * <p>付款建立失敗不應回滾訂單（訂單已持久化），
 * 而是透過 Saga DLQ 進行補償與重試。
 * 此監聽器捕獲所有例外並記錄日誌，確保不影響其他監聽器的執行。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener implements OrderEventListener {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 處理訂單建立事件：發送付款請求至 RabbitMQ
     *
     * <p>透過 RabbitMQ 向 payment-service 發送 Saga 付款請求，
     * 包含訂單 ID、金額等資訊，由 payment-service Consumer 接收後建立付款記錄。</p>
     *
     * @param event 訂單事件（預期為 {@link OrderCreatedEvent}）
     */
    @Override
    public void onOrderEvent(OrderEvent event) {
        if (!(event instanceof OrderCreatedEvent e)) return;

        Order order = e.getOrder();
        log.info("[PaymentEventListener] 收到訂單建立事件，orderId={}, amount={}",
                order.getId(), order.getTotalAmount());

        try {
            // 透過 RabbitMQ 發送 Saga 付款請求（非同步，不阻塞訂單流程）
            Map<String, Object> payload = Map.of(
                    "orderId",  order.getId(),
                    "orderNo",  order.getOrderNo() != null ? order.getOrderNo() : "",
                    "userId",   order.getUserId(),
                    "amount",   order.getTotalAmount()
            );
            rabbitTemplate.convertAndSend("saga.exchange", "payment.request", payload);
            log.info("[PaymentEventListener] 付款請求已發送至 RabbitMQ，orderId={}", order.getId());
        } catch (Exception ex) {
            // 捕獲例外，不回滾訂單，由 DLQ 重試機制處理
            log.error("[PaymentEventListener] 付款請求發送失敗，orderId={}，錯誤：{}",
                    order.getId(), ex.getMessage(), ex);
        }
    }

    /**
     * 此監聽器支援 {@link OrderCreatedEvent}
     *
     * @param eventClass 事件類型
     * @return {@code true} 僅當 eventClass 為 OrderCreatedEvent
     */
    @Override
    public boolean supports(Class<? extends OrderEvent> eventClass) {
        return OrderCreatedEvent.class.equals(eventClass);
    }
}

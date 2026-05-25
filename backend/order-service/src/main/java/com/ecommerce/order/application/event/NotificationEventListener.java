package com.ecommerce.order.application.event;

import com.ecommerce.order.domain.event.OrderCreatedEvent;
import com.ecommerce.order.domain.event.OrderEvent;
import com.ecommerce.order.domain.event.OrderEventListener;
import com.ecommerce.order.domain.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 通知事件監聽器（Observer Pattern - Concrete Observer）
 *
 * <p>監聽 {@link OrderCreatedEvent}，在訂單建立後併發發送 Email 與 SMS 通知。
 * 透過 RabbitMQ 將通知任務交給 notification-service 非同步處理。</p>
 *
 * <h3>通知方式</h3>
 * <ul>
 *   <li><b>Email</b>：發送訂單建立確認信</li>
 *   <li><b>SMS</b>：發送簡訊通知（若有手機號）</li>
 * </ul>
 *
 * <h3>容錯設計</h3>
 * <p>Email 或 SMS 發送失敗不影響訂單主流程，
 * 各自的例外獨立捕獲並記錄，不互相影響。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener implements OrderEventListener {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 處理訂單建立事件：併發發送 Email 與 SMS 通知
     *
     * <p>透過 {@link CompletableFuture#allOf} 併發發送兩種通知，
     * 縮短整體通知時間。各通知任務獨立捕獲例外，互不影響。</p>
     *
     * @param event 訂單事件（預期為 {@link OrderCreatedEvent}）
     */
    @Override
    public void onOrderEvent(OrderEvent event) {
        if (!(event instanceof OrderCreatedEvent e)) return;

        Order order = e.getOrder();
        log.info("[NotificationEventListener] 收到訂單建立事件，orderId={}", order.getId());

        // 併發發送 Email + SMS（互不阻塞）
        CompletableFuture<Void> emailFuture = CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> emailPayload = Map.of(
                        "type",    "ORDER_CREATED",
                        "orderId", order.getId(),
                        "orderNo", order.getOrderNo() != null ? order.getOrderNo() : "",
                        "userId",  order.getUserId(),
                        "amount",  order.getTotalAmount()
                );
                rabbitTemplate.convertAndSend("notification.exchange", "email.order.created", emailPayload);
                log.debug("[NotificationEventListener] Email 通知已發送，orderId={}", order.getId());
            } catch (Exception ex) {
                log.error("[NotificationEventListener] Email 通知發送失敗，orderId={}：{}",
                        order.getId(), ex.getMessage(), ex);
            }
        });

        CompletableFuture<Void> smsFuture = CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> smsPayload = Map.of(
                        "type",    "ORDER_CREATED",
                        "orderId", order.getId(),
                        "userId",  order.getUserId(),
                        "message", "您的訂單已建立，訂單號：" + (order.getOrderNo() != null ? order.getOrderNo() : order.getId())
                );
                rabbitTemplate.convertAndSend("notification.exchange", "sms.order.created", smsPayload);
                log.debug("[NotificationEventListener] SMS 通知已發送，orderId={}", order.getId());
            } catch (Exception ex) {
                log.error("[NotificationEventListener] SMS 通知發送失敗，orderId={}：{}",
                        order.getId(), ex.getMessage(), ex);
            }
        });

        // 等待兩個通知任務完成（設定合理逾時可加在此）
        CompletableFuture.allOf(emailFuture, smsFuture).join();
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

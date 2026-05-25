package com.ecommerce.order.application.event;

import com.ecommerce.order.domain.event.OrderEvent;
import com.ecommerce.order.domain.event.OrderEventListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 訂單事件發布器（Observer Pattern - Subject / Publisher）
 *
 * <p>持有所有 {@link OrderEventListener} Bean（由 Spring 自動注入），
 * 根據事件類型分派給支援的監聽器執行。</p>
 *
 * <h3>設計優點</h3>
 * <ul>
 *   <li><b>完全解耦：</b>{@code OrderApplicationService} 只呼叫 {@link #publish}，
 *       不需要知道有哪些監聽器存在</li>
 *   <li><b>容錯機制：</b>每個監聽器的例外獨立捕獲，不阻塞其他監聽器的執行</li>
 *   <li><b>自動發現：</b>新增 {@code @Component} 監聽器後自動被發現，
 *       無需修改此發布器</li>
 * </ul>
 *
 * <h3>事件分派流程</h3>
 * <pre>
 * publish(OrderCreatedEvent)
 *   → filter: supports(OrderCreatedEvent.class)
 *   → PaymentEventListener.onOrderEvent(event)
 *   → NotificationEventListener.onOrderEvent(event)
 *   → LoyaltyEventListener.onOrderEvent(event)
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    /**
     * 所有 {@link OrderEventListener} 實作，由 Spring 自動注入
     * （包含 PaymentEventListener、NotificationEventListener、LoyaltyEventListener）
     */
    private final List<OrderEventListener> listeners;

    /**
     * 發布訂單事件，分派給所有支援的監聽器
     *
     * <p>遍歷所有監聽器，呼叫 {@link OrderEventListener#supports} 過濾後執行。
     * 每個監聽器的例外都被獨立捕獲，確保一個監聽器失敗不影響其他監聽器。</p>
     *
     * @param event 要發布的訂單事件
     */
    public void publish(OrderEvent event) {
        String eventType = event.getClass().getSimpleName();
        log.info("[OrderEventPublisher] 發布事件：{}，eventId={}", eventType, event.getEventId());

        long count = listeners.stream()
                .filter(l -> l.supports(event.getClass()))
                .peek(listener -> {
                    try {
                        log.debug("[OrderEventPublisher] 分派事件 {} 至監聽器：{}",
                                eventType, listener.getClass().getSimpleName());
                        listener.onOrderEvent(event);
                    } catch (Exception e) {
                        // 不阻塞其他監聽器的執行
                        log.error("[OrderEventPublisher] 監聽器 {} 處理失敗，eventId={}：{}",
                                listener.getClass().getSimpleName(), event.getEventId(), e.getMessage(), e);
                    }
                })
                .count();

        log.info("[OrderEventPublisher] 事件 {} 已分派至 {} 個監聽器", eventType, count);
    }
}

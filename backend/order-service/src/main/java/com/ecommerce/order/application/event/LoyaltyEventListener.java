package com.ecommerce.order.application.event;

import com.ecommerce.order.domain.event.OrderCreatedEvent;
import com.ecommerce.order.domain.event.OrderEvent;
import com.ecommerce.order.domain.event.OrderEventListener;
import com.ecommerce.order.domain.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 積分事件監聽器（Observer Pattern - Concrete Observer）
 *
 * <p>監聽 {@link OrderCreatedEvent}，在訂單建立後為會員計算並累積積分。
 * 此監聽器為「新增功能」的典型範例——無需修改 {@code OrderApplicationService}，
 * 只需新增此類別即可擴充系統功能，體現觀察者模式的零修改擴充能力。</p>
 *
 * <h3>積分計算規則</h3>
 * <p>每消費 1 元獲得 0.1 積分（整數部分），
 * 例如消費 250 元 → 25 積分。</p>
 *
 * <h3>設計說明</h3>
 * <p>積分累積透過 RabbitMQ 非同步傳送至 loyalty-service 處理，
 * 即使 loyalty-service 暫時不可用，訊息也會保存在 Queue 中待恢復後處理。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoyaltyEventListener implements OrderEventListener {

    /** 積分換算率：每 1 元 = 0.1 積分 */
    private static final BigDecimal POINTS_RATE = new BigDecimal("0.1");

    private final RabbitTemplate rabbitTemplate;

    /**
     * 處理訂單建立事件：計算並累積會員積分
     *
     * <p>依訂單金額計算積分（金額 × 0.1，取整數），
     * 透過 RabbitMQ 發送積分累積請求至 loyalty-service。</p>
     *
     * @param event 訂單事件（預期為 {@link OrderCreatedEvent}）
     */
    @Override
    public void onOrderEvent(OrderEvent event) {
        if (!(event instanceof OrderCreatedEvent e)) return;

        Order order = e.getOrder();
        log.info("[LoyaltyEventListener] 收到訂單建立事件，orderId={}, amount={}",
                order.getId(), order.getTotalAmount());

        try {
            // 計算積分（金額 × 0.1，取整數部分）
            int points = order.getTotalAmount()
                    .multiply(POINTS_RATE)
                    .intValue();

            if (points <= 0) {
                log.debug("[LoyaltyEventListener] 積分為 0，略過，orderId={}", order.getId());
                return;
            }

            Map<String, Object> payload = Map.of(
                    "userId",  order.getUserId(),
                    "orderId", order.getId(),
                    "points",  points,
                    "reason",  "訂單消費積分"
            );
            rabbitTemplate.convertAndSend("loyalty.exchange", "points.add", payload);
            log.info("[LoyaltyEventListener] 積分累積請求已發送，userId={}, points={}, orderId={}",
                    order.getUserId(), points, order.getId());
        } catch (Exception ex) {
            // 積分失敗不影響訂單，記錄日誌後靜默處理
            log.error("[LoyaltyEventListener] 積分累積失敗，orderId={}：{}",
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

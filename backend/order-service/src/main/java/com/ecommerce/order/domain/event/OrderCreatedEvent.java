package com.ecommerce.order.domain.event;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.interfaces.dto.CreateOrderRequest;

/**
 * 訂單建立事件（Observer Pattern - Concrete Event）
 *
 * <p>當新訂單成功持久化後，由 {@code OrderApplicationService#createOrder}
 * 透過 {@link com.ecommerce.order.application.event.OrderEventPublisher} 發布此事件。</p>
 *
 * <h3>訂閱此事件的監聽器</h3>
 * <ul>
 *   <li>{@link com.ecommerce.order.application.event.PaymentEventListener}
 *       - 建立對應付款記錄</li>
 *   <li>{@link com.ecommerce.order.application.event.NotificationEventListener}
 *       - 發送訂單建立通知（Email + SMS）</li>
 *   <li>{@link com.ecommerce.order.application.event.LoyaltyEventListener}
 *       - 計算並累積會員積分</li>
 * </ul>
 *
 * <p><b>設計原則：</b>
 * {@code OrderApplicationService} 只負責發布此事件，
 * 不知道有哪些監聽器訂閱，達到完全解耦。</p>
 */
public class OrderCreatedEvent extends OrderEvent {

    /** 已建立並持久化的訂單（含資料庫 ID 與訂單號） */
    private final Order order;

    /** 原始建立請求（供監聽器取得額外資訊，如 recipientEmail） */
    private final CreateOrderRequest request;

    /**
     * 建立訂單建立事件
     *
     * @param order   已儲存到資料庫的訂單物件（含 ID）
     * @param request 原始建立請求 DTO
     */
    public OrderCreatedEvent(Order order, CreateOrderRequest request) {
        this.order   = order;
        this.request = request;
    }

    /**
     * 取得已建立的訂單
     *
     * @return 含資料庫 ID 的訂單領域物件
     */
    public Order getOrder() {
        return order;
    }

    /**
     * 取得原始建立請求
     *
     * @return 建立訂單時的請求 DTO
     */
    public CreateOrderRequest getRequest() {
        return request;
    }
}

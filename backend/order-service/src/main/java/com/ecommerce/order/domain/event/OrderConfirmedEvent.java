package com.ecommerce.order.domain.event;

import com.ecommerce.order.domain.model.Order;

/**
 * 訂單確認事件（Observer Pattern - Concrete Event）
 *
 * <p>當 Saga 流程完成（庫存扣減 + 付款成功），訂單狀態更新為 CONFIRMED 後發布此事件。
 * 主要用於通知下游服務開始揀貨備貨。</p>
 *
 * <h3>典型訂閱場景</h3>
 * <ul>
 *   <li>通知 WMS（倉管系統）開始備貨</li>
 *   <li>發送「訂單確認」Email 通知給用戶</li>
 *   <li>更新推薦系統的用戶購買記錄</li>
 * </ul>
 */
public class OrderConfirmedEvent extends OrderEvent {

    /** 已確認的訂單 */
    private final Order order;

    /**
     * 建立訂單確認事件
     *
     * @param order 狀態已更新為 CONFIRMED 的訂單物件
     */
    public OrderConfirmedEvent(Order order) {
        this.order = order;
    }

    /**
     * 取得已確認的訂單
     *
     * @return 訂單領域物件
     */
    public Order getOrder() {
        return order;
    }
}

package com.ecommerce.order.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 訂單領域事件抽象基類（Observer Pattern - Event Base）
 *
 * <p>所有訂單相關事件的共同父類別，提供事件唯一識別碼與時間戳。
 * 事件屬於 Domain 層，不依賴任何框架（純 Java）。</p>
 *
 * <h3>現有事件列表</h3>
 * <ul>
 *   <li>{@link OrderCreatedEvent}   - 訂單建立後觸發</li>
 *   <li>{@link OrderConfirmedEvent} - 訂單確認後觸發（Saga 完成）</li>
 * </ul>
 *
 * <h3>新增事件步驟</h3>
 * <ol>
 *   <li>繼承此類別，新增業務欄位</li>
 *   <li>新增對應的 {@link OrderEventListener} 實作</li>
 *   <li>在業務方法中透過 {@link com.ecommerce.order.application.event.OrderEventPublisher}
 *       發布事件</li>
 * </ol>
 */
public abstract class OrderEvent {

    /** 事件唯一識別碼（UUID），用於冪等性檢查與日誌追蹤 */
    private final String eventId = UUID.randomUUID().toString();

    /** 事件發生時間 */
    private final LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 取得事件唯一識別碼
     *
     * @return UUID 字串格式的事件 ID
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * 取得事件發生時間
     *
     * @return 事件建立時的時間戳
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}

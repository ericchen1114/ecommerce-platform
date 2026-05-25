package com.ecommerce.order.domain.event;

/**
 * 訂單事件監聽器介面（Observer Pattern - Observer）
 *
 * <p>定義事件監聽器的統一合約，所有需要響應訂單事件的業務邏輯
 * 均應實作此介面，並以 {@code @Component} 標注以被 Spring 自動發現。</p>
 *
 * <h3>現有監聽器</h3>
 * <ul>
 *   <li>{@link com.ecommerce.order.application.event.PaymentEventListener}
 *       - 訂單建立後自動建立付款記錄</li>
 *   <li>{@link com.ecommerce.order.application.event.NotificationEventListener}
 *       - 訂單建立後發送 Email + SMS 通知</li>
 *   <li>{@link com.ecommerce.order.application.event.LoyaltyEventListener}
 *       - 訂單建立後累積會員積分</li>
 * </ul>
 *
 * <h3>新增監聽器步驟（零修改 OrderApplicationService）</h3>
 * <ol>
 *   <li>建立新類別實作此介面</li>
 *   <li>在 {@link #supports(Class)} 回傳要監聽的事件類型</li>
 *   <li>在 {@link #onOrderEvent(OrderEvent)} 實作業務邏輯</li>
 *   <li>加上 {@code @Component} 讓 Spring 自動注入</li>
 * </ol>
 */
public interface OrderEventListener {

    /**
     * 處理訂單事件
     *
     * <p>實作時應確保：
     * <ul>
     *   <li>方法內的例外不向外拋出（記錄日誌即可），避免影響其他監聽器</li>
     *   <li>業務邏輯冪等，因為事件可能因重試而多次觸發</li>
     *   <li>透過 {@code instanceof} 轉型後再取得具體欄位</li>
     * </ul>
     * </p>
     *
     * @param event 訂單事件（具體類型由 {@link #supports(Class)} 決定）
     */
    void onOrderEvent(OrderEvent event);

    /**
     * 判斷此監聽器是否支援指定事件類型
     *
     * <p>由 {@link com.ecommerce.order.application.event.OrderEventPublisher}
     * 呼叫此方法決定是否將事件分派給此監聽器。</p>
     *
     * @param eventClass 事件類型（如 {@code OrderCreatedEvent.class}）
     * @return           {@code true} 表示此監聽器可處理該事件類型
     */
    boolean supports(Class<? extends OrderEvent> eventClass);
}

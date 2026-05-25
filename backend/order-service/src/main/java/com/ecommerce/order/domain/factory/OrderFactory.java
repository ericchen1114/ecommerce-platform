package com.ecommerce.order.domain.factory;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.interfaces.dto.CreateOrderRequest;

/**
 * 訂單工廠介面（Factory Pattern）
 *
 * <p>定義訂單建立的統一合約，不同訂單類型由各自的工廠實作類別實現此介面，
 * 將訂單建立邏輯從 {@code OrderApplicationService} 中分離出來，
 * 達到對修改封閉、對擴充開放（OCP）的效果。</p>
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * // 由 OrderFactorySelector 自動選取對應工廠
 * OrderFactory factory = selector.getFactory(req.getOrderType());
 * Order order = factory.create(req);
 * }</pre>
 *
 * <h3>新增訂單類型步驟</h3>
 * <ol>
 *   <li>在 {@link OrderType} 新增列舉值</li>
 *   <li>新增實作此介面的 {@code @Component} 類別</li>
 *   <li>覆寫 {@link #supports(OrderType)} 回傳對應類型</li>
 * </ol>
 */
public interface OrderFactory {

    /**
     * 根據請求建立對應類型的訂單領域物件
     *
     * <p>工廠負責：業務規則驗證、物件初始化、狀態設定。
     * 不負責：持久化、價格計算（交由策略模式處理）。</p>
     *
     * @param req 建立訂單的請求 DTO
     * @return    已初始化的 {@link Order} 領域物件（尚未持久化）
     * @throws com.ecommerce.order.domain.exception.BusinessException 業務規則驗證失敗時
     */
    Order create(CreateOrderRequest req);

    /**
     * 判斷此工廠是否支援指定的訂單類型
     *
     * <p>{@link com.ecommerce.order.application.factory.OrderFactorySelector}
     * 透過此方法在所有 {@code OrderFactory} Bean 中選取正確的工廠。</p>
     *
     * @param type 欲建立的訂單類型
     * @return     {@code true} 表示此工廠可處理該類型
     */
    boolean supports(OrderType type);
}

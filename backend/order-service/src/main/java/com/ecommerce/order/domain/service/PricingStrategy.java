package com.ecommerce.order.domain.service;

import com.ecommerce.order.domain.model.Order;

import java.math.BigDecimal;

/**
 * 計價策略介面（Strategy Pattern）
 *
 * <p>定義價格計算的統一合約，不同的計價規則（地區運費、會員折扣、促銷碼、稅率）
 * 各自實作此介面，由 {@link com.ecommerce.order.application.pricing.PricingContext}
 * 依序組合執行。</p>
 *
 * <h3>策略執行順序（重要）</h3>
 * <pre>
 * 基礎金額
 *   → MemberDiscountStrategy（先扣折扣）
 *   → PromotionStrategy（再扣促銷）
 *   → RegionalPricingStrategy（加地區運費）
 *   → TaxStrategy（最後加稅）
 * </pre>
 *
 * <h3>擴充說明</h3>
 * <p>新增計價規則只需：
 * <ol>
 *   <li>實作此介面</li>
 *   <li>標注 {@code @Component}</li>
 *   <li>在 {@link com.ecommerce.order.application.pricing.PricingContext} 中加入執行順序</li>
 * </ol>
 * </p>
 *
 * <p>此介面屬於 Domain 層（Domain Service），不依賴任何框架。</p>
 */
public interface PricingStrategy {

    /**
     * 將此計價規則套用到當前價格，回傳調整後的金額
     *
     * <p>實作時應確保：
     * <ul>
     *   <li>不修改 {@code order} 物件（只讀取）</li>
     *   <li>金額計算使用 {@link BigDecimal} 避免浮點誤差</li>
     *   <li>若此策略不適用（如非會員不套用折扣），直接回傳 {@code basePrice}</li>
     * </ul>
     * </p>
     *
     * @param basePrice 套用此策略前的當前金額
     * @param order     訂單領域物件（提供地區、會員等級等計算依據）
     * @return          套用此策略後的金額
     */
    BigDecimal apply(BigDecimal basePrice, Order order);

    /**
     * 取得策略名稱（用於日誌記錄）
     *
     * @return 策略中文名稱，如 "會員折扣"、"地區運費"
     */
    String getName();
}

package com.ecommerce.order.interfaces.dto;

import com.ecommerce.order.domain.factory.OrderType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 建立訂單 API 請求 DTO
 *
 * <p>前端透過 {@code POST /api/orders} 提交此物件；
 * Controller 驗證後傳入 {@code OrderApplicationService#createOrder}，
 * 再由 {@link com.ecommerce.order.application.factory.OrderFactorySelector}
 * 選取對應工廠建立訂單。</p>
 *
 * <h3>欄位說明</h3>
 * <ul>
 *   <li>{@code orderType}  - 必填，決定使用哪個工廠與哪些欄位有效</li>
 *   <li>{@code productId}  - STANDARD 類型必填</li>
 *   <li>{@code quantity}   - STANDARD 類型必填，最小為 1</li>
 *   <li>{@code giftCardValue}    - GIFT_CARD 類型必填，禮品卡面值</li>
 *   <li>{@code recipientEmail}   - GIFT_CARD 類型必填，收件人 Email</li>
 *   <li>{@code planId}           - SUBSCRIPTION 類型必填，訂閱方案 ID</li>
 *   <li>{@code region}           - 地區代碼，用於地區運費計算（如 "NYC", "LA"）</li>
 *   <li>{@code memberLevel}      - 會員等級（"GOLD"/"SILVER"/"BRONZE"）</li>
 *   <li>{@code promoCode}        - 促銷碼，選填</li>
 *   <li>{@code shippingState}    - 寄送州/地區，用於稅率計算</li>
 *   <li>{@code shippingAddress}  - 完整寄送地址</li>
 *   <li>{@code note}             - 訂單備註，選填</li>
 * </ul>
 */
@Data
public class CreateOrderRequest {

    /** 訂單類型，決定工廠選擇與業務規則 */
    @NotNull(message = "訂單類型不可為空")
    private OrderType orderType;

    /** 下單會員 ID（由 Gateway JWT 解析後注入，Controller 從 Header 取得） */
    private Long userId;

    // ─── STANDARD 訂單欄位 ─────────────────────────────────────────────────────

    /** 商品 ID（STANDARD 類型必填） */
    private Long productId;

    /** 購買數量（STANDARD 類型必填，最小 1） */
    @Min(value = 1, message = "購買數量最小為 1")
    private Integer quantity;

    // ─── GIFT_CARD 訂單欄位 ────────────────────────────────────────────────────

    /** 禮品卡面值（GIFT_CARD 類型必填） */
    private BigDecimal giftCardValue;

    /** 禮品卡收件人 Email（GIFT_CARD 類型必填） */
    private String recipientEmail;

    // ─── SUBSCRIPTION 訂單欄位 ─────────────────────────────────────────────────

    /** 訂閱方案 ID（SUBSCRIPTION 類型必填） */
    private Long planId;

    // ─── 通用計價欄位（策略模式使用）──────────────────────────────────────────

    /** 地區代碼，用於地區運費策略（如 "NYC"、"LA"、"MIDWEST"） */
    private String region;

    /** 會員等級，用於折扣策略（"GOLD"、"SILVER"、"BRONZE"） */
    private String memberLevel;

    /** 是否為會員（true 才套用會員折扣） */
    private boolean member;

    /** 促銷碼，選填，由促銷策略驗證有效性 */
    private String promoCode;

    /** 寄送州/地區代碼，用於稅率策略 */
    private String shippingState;

    /** 完整寄送地址 */
    private String shippingAddress;

    /** 訂單備註 */
    private String note;
}

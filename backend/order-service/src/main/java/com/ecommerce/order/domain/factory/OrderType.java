package com.ecommerce.order.domain.factory;

/**
 * 訂單類型列舉
 *
 * <p>定義系統支援的所有訂單類型，每種類型對應不同的建立邏輯（由工廠模式實作）：
 * <ul>
 *   <li>{@link #STANDARD}    - 一般商品訂單，需驗證庫存</li>
 *   <li>{@link #GIFT_CARD}   - 禮品卡訂單，不涉及庫存，需指定收件人 Email</li>
 *   <li>{@link #SUBSCRIPTION} - 訂閱方案訂單，含自動續約日期</li>
 * </ul>
 * </p>
 *
 * <p>新增訂單類型時，只需：
 * <ol>
 *   <li>在此列舉加入新成員</li>
 *   <li>新增對應的 {@code OrderFactory} 實作並以 {@code @Component} 標注</li>
 * </ol>
 * 無需修改 {@code OrderApplicationService}，符合開閉原則（OCP）。</p>
 */
public enum OrderType {

    /** 標準商品訂單 */
    STANDARD("標準訂單"),

    /** 禮品卡訂單 */
    GIFT_CARD("禮品卡"),

    /** 訂閱方案訂單 */
    SUBSCRIPTION("訂閱");

    private final String description;

    /**
     * @param description 中文描述，用於日誌與錯誤訊息
     */
    OrderType(String description) {
        this.description = description;
    }

    /**
     * 取得此訂單類型的中文描述
     *
     * @return 中文描述字串
     */
    public String getDescription() {
        return description;
    }
}

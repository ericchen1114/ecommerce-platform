package com.ecommerce.order.domain.model;

import com.ecommerce.order.domain.factory.OrderType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單聚合根（Aggregate Root）
 *
 * <p>封裝訂單的所有業務規則與狀態轉換。
 * 此類別屬於 Domain 層，不依賴任何框架（純 Java）。</p>
 *
 * <h3>工廠模式整合</h3>
 * <p>請勿直接使用建構子，應透過 {@link com.ecommerce.order.domain.factory.OrderFactory}
 * 的實作類別建立訂單，以確保業務規則在建立時被正確驗證。</p>
 *
 * <h3>狀態轉換</h3>
 * <pre>
 * PENDING → CONFIRMED → PROCESSING → SHIPPED → DELIVERED
 *         ↘                                   ↗
 *           CANCELLED（任何階段皆可取消）
 * </pre>
 */
@Getter
@Builder
public class Order {

    /** 資料庫主鍵（由 JPA 生成，新建訂單時為 null） */
    private Long id;

    /** 訂單編號（格式：ORD + yyyyMMdd + 6位序號，如 ORD20260522000001） */
    private String orderNo;

    /** 下單會員 ID */
    private Long userId;

    /** 訂單類型，決定建立邏輯與計價規則 */
    private OrderType orderType;

    /** 訂單金額（含折扣與稅後最終金額） */
    @Setter
    private BigDecimal totalAmount;

    /** 訂單狀態 */
    @Setter
    private OrderStatus status;

    /** 寄送地址 */
    private String shippingAddress;

    /** 地區代碼，用於運費計算（如 "NYC"、"LA"） */
    private String region;

    /** 會員等級（"GOLD"/"SILVER"/"BRONZE"），影響折扣策略 */
    private String memberLevel;

    /** 是否為會員，決定是否套用會員折扣 */
    private boolean member;

    /** 促銷碼，套用後由促銷策略計算折扣 */
    private String promoCode;

    /** 寄送州/地區代碼，用於稅率計算 */
    private String shippingState;

    /** 訂單備註 */
    private String note;

    /** 訂單明細（一對多，懶加載） */
    private List<OrderItem> items;

    /** 建立時間 */
    private LocalDateTime createdAt;

    /**
     * 建立標準訂單（STANDARD 類型）的便捷工廠方法
     *
     * <p>此方法保留向下相容，新訂單類型請使用 {@link com.ecommerce.order.domain.factory.OrderFactory}。</p>
     *
     * @param userId          下單會員 ID
     * @param shippingAddress 寄送地址
     * @param note            訂單備註
     * @param items           訂單明細列表
     * @return                已初始化的 {@code Order}（狀態為 PENDING）
     */
    public static Order create(Long userId, String shippingAddress, String note, List<OrderItem> items) {
        BigDecimal total = items.stream()
                .map(i -> i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Order.builder()
                .userId(userId)
                .orderType(OrderType.STANDARD)
                .shippingAddress(shippingAddress)
                .note(note)
                .items(items)
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 確認訂單
     *
     * <p>將訂單狀態從 {@code PENDING} 轉為 {@code CONFIRMED}。
     * 僅 PENDING 狀態的訂單可確認，否則拋出例外。</p>
     *
     * @throws IllegalStateException 訂單狀態不為 PENDING 時拋出
     */
    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("只有待處理訂單可確認");
        }
        this.status = OrderStatus.CONFIRMED;
    }

    /**
     * 取消訂單
     *
     * <p>將訂單狀態轉為 {@code CANCELLED}。
     * 已取消或已完成的訂單不可再次取消。</p>
     *
     * @throws IllegalStateException 訂單已取消或已完成時拋出
     */
    public void cancel() {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("此訂單無法取消，當前狀態：" + this.status);
        }
        this.status = OrderStatus.CANCELLED;
    }

    /** 訂單狀態 */
    public enum OrderStatus {
        /** 待處理（初始狀態） */
        PENDING,
        /** 已確認（已驗證庫存與付款） */
        CONFIRMED,
        /** 處理中（備貨中） */
        PROCESSING,
        /** 已出貨 */
        SHIPPED,
        /** 已完成 */
        DELIVERED,
        /** 已取消 */
        CANCELLED
    }
}

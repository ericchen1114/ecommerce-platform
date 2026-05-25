package com.ecommerce.order.infrastructure.persistence.jpa;

import com.ecommerce.order.domain.factory.OrderType;
import com.ecommerce.order.domain.model.Order;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單 JPA Entity（Infrastructure Layer）
 *
 * <p>對應資料庫 {@code orders} 表，負責 ORM 映射。
 * 不包含任何業務邏輯，轉換邏輯由
 * {@link com.ecommerce.order.infrastructure.persistence.mapper.OrderEntityMapper} 處理（MapStruct 自動生成）。</p>
 */
@Data
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_order_no", columnList = "orderNo", unique = true),
    @Index(name = "idx_orders_user_id",  columnList = "userId")
})
public class OrderJpaEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 對外顯示的訂單編號（ORD + yyyyMMdd + 6位序號） */
    @Column(name = "order_no", unique = true, length = 20)
    private String orderNo;

    private Long userId;

    /** 訂單類型（STANDARD / GIFT_CARD / SUBSCRIPTION） */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private OrderType orderType;

    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private Order.OrderStatus status;

    private String shippingAddress;

    /** 地區代碼（運費計算用） */
    @Column(length = 10)
    private String region;

    /** 會員等級（折扣策略用） */
    @Column(length = 10)
    private String memberLevel;

    /** 是否為會員 */
    private boolean member;

    /** 促銷碼 */
    @Column(length = 20)
    private String promoCode;

    /** 寄送州/地區代碼（稅率計算用） */
    @Column(length = 10)
    private String shippingState;

    private String note;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItemJpaEntity> items;

    private LocalDateTime createdAt;
}

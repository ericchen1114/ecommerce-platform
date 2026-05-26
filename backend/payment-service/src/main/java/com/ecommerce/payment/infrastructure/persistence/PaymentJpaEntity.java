package com.ecommerce.payment.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 付款記錄 JPA Entity
 *
 * <p>對應 MySQL {@code payments} 資料表，
 * 由 JPA {@code ddl-auto: update} 自動建立。</p>
 */
@Entity
@Table(name = "payments",
       indexes = @Index(name = "idx_order_id", columnList = "orderId"))
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** PENDING / COMPLETED / FAILED / REFUNDED */
    @Column(nullable = false, length = 20)
    private String status;

    /** CREDIT_CARD / LINE_PAY / CASH_ON_DELIVERY */
    @Column(length = 30)
    private String paymentMethod;

    /** 金流交易 ID（第三方回傳）*/
    @Column(length = 100)
    private String transactionId;

    /** 失敗原因 */
    @Column(length = 255)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

package com.ecommerce.order.infrastructure.messaging;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Saga 事件訊息 DTO（序列化為 JSON 透過 RabbitMQ 傳遞）
 *
 * <h3>訊息流向</h3>
 * <pre>
 * order-service   → payment.request  → payment-service
 * payment-service → payment.result   → order-service
 * order-service   → stock.request    → product-service
 * product-service → stock.result     → order-service
 * order-service   → notification     → notification-service
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaMessage {

    /** 訊息唯一 ID（冪等性保證） */
    private String messageId;

    /** 事件類型：PAYMENT_REQUEST / PAYMENT_COMPLETED / PAYMENT_FAILED /
     *            STOCK_REQUEST / STOCK_RESERVED / STOCK_FAILED /
     *            ORDER_CONFIRMED / ORDER_CANCELLED / NOTIFICATION */
    private String eventType;

    /** 訂單 ID */
    private Long orderId;

    /** 訂單號（業務可讀） */
    private String orderNo;

    /** 用戶 ID */
    private Long userId;

    /** 金額 */
    private BigDecimal amount;

    /** 商品 ID（庫存操作用） */
    private String productId;

    /** 庫存數量（庫存操作用） */
    private Integer quantity;

    /** 失敗原因（補償訊息用） */
    private String failureReason;

    /** 通知內容（notification 訊息用） */
    private String notificationContent;

    /** 訊息建立時間 */
    private LocalDateTime createdAt;

    // ─── Factory methods ──────────────────────────────────────────────────────

    public static SagaMessage paymentRequest(Long orderId, String orderNo, Long userId, BigDecimal amount) {
        return SagaMessage.builder()
                .messageId(java.util.UUID.randomUUID().toString())
                .eventType("PAYMENT_REQUEST")
                .orderId(orderId).orderNo(orderNo)
                .userId(userId).amount(amount)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static SagaMessage paymentCompleted(Long orderId, String orderNo) {
        return SagaMessage.builder()
                .messageId(java.util.UUID.randomUUID().toString())
                .eventType("PAYMENT_COMPLETED")
                .orderId(orderId).orderNo(orderNo)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static SagaMessage paymentFailed(Long orderId, String orderNo, String reason) {
        return SagaMessage.builder()
                .messageId(java.util.UUID.randomUUID().toString())
                .eventType("PAYMENT_FAILED")
                .orderId(orderId).orderNo(orderNo)
                .failureReason(reason)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static SagaMessage notification(Long orderId, String orderNo, Long userId, String content) {
        return SagaMessage.builder()
                .messageId(java.util.UUID.randomUUID().toString())
                .eventType("NOTIFICATION")
                .orderId(orderId).orderNo(orderNo)
                .userId(userId).notificationContent(content)
                .createdAt(LocalDateTime.now())
                .build();
    }
}

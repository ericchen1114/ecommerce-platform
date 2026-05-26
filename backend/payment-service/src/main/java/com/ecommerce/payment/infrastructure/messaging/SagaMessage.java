package com.ecommerce.payment.infrastructure.messaging;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Saga 事件訊息 DTO（payment-service 用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaMessage {
    private String messageId;
    private String eventType;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private BigDecimal amount;
    private String failureReason;
    private LocalDateTime createdAt;

    public static SagaMessage completed(Long orderId, String orderNo) {
        return SagaMessage.builder()
                .messageId(java.util.UUID.randomUUID().toString())
                .eventType("PAYMENT_COMPLETED")
                .orderId(orderId).orderNo(orderNo)
                .createdAt(LocalDateTime.now()).build();
    }

    public static SagaMessage failed(Long orderId, String orderNo, String reason) {
        return SagaMessage.builder()
                .messageId(java.util.UUID.randomUUID().toString())
                .eventType("PAYMENT_FAILED")
                .orderId(orderId).orderNo(orderNo)
                .failureReason(reason)
                .createdAt(LocalDateTime.now()).build();
    }
}

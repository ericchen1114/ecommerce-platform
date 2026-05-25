package com.ecommerce.payment.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class Payment {
    private Long id;
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private PaymentStatus status;
    private PaymentMethod method;
    private String transactionId;
    private LocalDateTime createdAt;

    public static Payment create(Long orderId, Long userId, BigDecimal amount, PaymentMethod method) {
        return Payment.builder()
            .orderId(orderId).userId(userId).amount(amount)
            .method(method).status(PaymentStatus.PENDING)
            .createdAt(LocalDateTime.now()).build();
    }

    public enum PaymentStatus { PENDING, COMPLETED, FAILED, REFUNDED }
    public enum PaymentMethod { CREDIT_CARD, DEBIT_CARD, LINE_PAY, CASH_ON_DELIVERY }
}

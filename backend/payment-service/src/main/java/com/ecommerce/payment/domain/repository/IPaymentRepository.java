package com.ecommerce.payment.domain.repository;

import com.ecommerce.payment.domain.model.Payment;
import java.util.Optional;

/**
 * 付款 Repository 介面（Domain Layer）
 *
 * <p>Domain 層只依賴此介面，不依賴 JPA 實作，
 * 符合 DDD 依賴反轉原則。</p>
 */
public interface IPaymentRepository {
    Payment save(Payment payment);
    Optional<Payment> findByOrderId(Long orderId);
    boolean existsCompletedPayment(Long orderId);
}

package com.ecommerce.payment.infrastructure.persistence;

import com.ecommerce.payment.domain.model.Payment;
import com.ecommerce.payment.domain.repository.IPaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 付款 Repository JPA 實作
 */
@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements IPaymentRepository {

    private final PaymentJpaRepository jpaRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = toEntity(payment);
        PaymentJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId).map(this::toDomain);
    }

    @Override
    public boolean existsCompletedPayment(Long orderId) {
        return jpaRepository.existsByOrderIdAndStatus(orderId, "COMPLETED");
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private PaymentJpaEntity toEntity(Payment p) {
        return PaymentJpaEntity.builder()
                .id(p.getId())
                .orderId(p.getOrderId())
                .userId(p.getUserId())
                .amount(p.getAmount())
                .status(p.getStatus() != null ? p.getStatus().name() : "PENDING")
                .paymentMethod(p.getMethod() != null ? p.getMethod().name() : null)
                .transactionId(p.getTransactionId())
                .build();
    }

    private Payment toDomain(PaymentJpaEntity e) {
        return Payment.builder()
                .id(e.getId())
                .orderId(e.getOrderId())
                .userId(e.getUserId())
                .amount(e.getAmount())
                .status(Payment.PaymentStatus.valueOf(e.getStatus()))
                .method(e.getPaymentMethod() != null
                        ? Payment.PaymentMethod.valueOf(e.getPaymentMethod()) : null)
                .transactionId(e.getTransactionId())
                .createdAt(e.getCreatedAt())
                .build();
    }
}

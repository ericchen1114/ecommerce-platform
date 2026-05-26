package com.ecommerce.payment.application.service;

import com.ecommerce.payment.domain.model.Payment;
import com.ecommerce.payment.domain.repository.IPaymentRepository;
import com.ecommerce.payment.infrastructure.messaging.SagaMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static com.ecommerce.payment.infrastructure.messaging.SagaRabbitMQConfig.*;

/**
 * 付款應用服務（Application Service）
 *
 * <h3>付款流程</h3>
 * <pre>
 * 1. 冪等檢查（同一訂單不重複付款）
 * 2. 建立 PENDING 付款記錄
 * 3. 模擬金流處理（實際應接第三方金流 SDK）
 * 4. 更新付款狀態 COMPLETED / FAILED
 * 5. 發布結果到 payment.result.queue
 * </pre>
 *
 * <h3>冪等性</h3>
 * <p>同一 orderId 若已有 COMPLETED 付款記錄，直接重發 PAYMENT_COMPLETED 訊息，
 * 確保 Saga 消費端重試時不重複扣款。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentApplicationService {

    private final IPaymentRepository paymentRepository;
    private final RabbitTemplate     rabbitTemplate;

    /**
     * 處理付款請求（Saga Consumer 呼叫）
     *
     * @param orderId   訂單 ID
     * @param orderNo   訂單號
     * @param userId    用戶 ID
     * @param amount    付款金額
     * @param method    付款方式
     */
    @Transactional
    public void processPayment(Long orderId, String orderNo, Long userId,
                               BigDecimal amount, String method) {

        // ① 冪等：已成功付款則重發完成訊息
        if (paymentRepository.existsCompletedPayment(orderId)) {
            log.info("[PaymentService] 訂單 {} 已付款，重發 COMPLETED 訊息（冪等）", orderId);
            publishResult(SagaMessage.completed(orderId, orderNo));
            return;
        }

        // ② 建立 PENDING 付款記錄
        Payment payment = Payment.create(orderId, userId, amount,
                method != null ? Payment.PaymentMethod.valueOf(method) : Payment.PaymentMethod.CREDIT_CARD);
        Payment saved = paymentRepository.save(payment);
        log.info("[PaymentService] 付款記錄已建立，paymentId={}, orderId={}", saved.getId(), orderId);

        // ③ 模擬金流處理（實際應呼叫第三方 SDK）
        try {
            String txId = simulatePaymentGateway(amount);

            // ④ 更新為 COMPLETED
            Payment completed = Payment.builder()
                    .id(saved.getId()).orderId(orderId).userId(userId)
                    .amount(amount)
                    .status(Payment.PaymentStatus.COMPLETED)
                    .method(saved.getMethod())
                    .transactionId(txId)
                    .createdAt(saved.getCreatedAt())
                    .build();
            paymentRepository.save(completed);
            log.info("[PaymentService] 付款成功，txId={}, orderId={}", txId, orderId);

            // ⑤ 發布 PAYMENT_COMPLETED
            publishResult(SagaMessage.completed(orderId, orderNo));

        } catch (Exception e) {
            // 更新為 FAILED
            Payment failed = Payment.builder()
                    .id(saved.getId()).orderId(orderId).userId(userId)
                    .amount(amount)
                    .status(Payment.PaymentStatus.FAILED)
                    .method(saved.getMethod())
                    .createdAt(saved.getCreatedAt())
                    .build();
            paymentRepository.save(failed);
            log.error("[PaymentService] 付款失敗，orderId={}：{}", orderId, e.getMessage());

            // 發布 PAYMENT_FAILED（觸發 Saga 補償）
            publishResult(SagaMessage.failed(orderId, orderNo, e.getMessage()));
        }
    }

    private void publishResult(SagaMessage message) {
        rabbitTemplate.convertAndSend(SAGA_EXCHANGE, RK_PAYMENT_RESULT, message);
        log.info("[PaymentService] 發布付款結果 {}，orderId={}", message.getEventType(), message.getOrderId());
    }

    /**
     * 模擬金流閘道（實際應替換為 TapPay / 綠界 / LinePay SDK）
     *
     * @return 交易 ID
     */
    private String simulatePaymentGateway(BigDecimal amount) {
        // 模擬 95% 成功率（生產環境替換為真實金流）
        if (Math.random() < 0.05) {
            throw new RuntimeException("金流閘道連線逾時");
        }
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

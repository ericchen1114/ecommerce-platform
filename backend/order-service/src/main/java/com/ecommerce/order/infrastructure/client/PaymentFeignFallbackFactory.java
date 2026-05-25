package com.ecommerce.order.infrastructure.client;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Log4j2
@Component
public class PaymentFeignFallbackFactory implements FallbackFactory<PaymentFeignClient> {

    @Override
    public PaymentFeignClient create(Throwable cause) {
        return new PaymentFeignClient() {

            @Override
            public Map<String, Object> createPayment(Map<String, Object> request) {
                log.warn("[FEIGN FALLBACK] payment-service createPayment 失敗: {}", cause.getMessage());
                // 觸發 Saga 補償流程
                return Map.of(
                    "success", false,
                    "message", "付款服務暫時無法使用，已觸發補償流程",
                    "circuitBreaker", "OPEN"
                );
            }

            @Override
            public Map<String, Object> getPaymentByOrder(Long orderId) {
                log.warn("[FEIGN FALLBACK] payment-service getPaymentByOrder={} 失敗", orderId);
                return Map.of("orderId", orderId, "status", "UNKNOWN");
            }

            @Override
            public String health() {
                return "payment-service unavailable";
            }
        };
    }
}

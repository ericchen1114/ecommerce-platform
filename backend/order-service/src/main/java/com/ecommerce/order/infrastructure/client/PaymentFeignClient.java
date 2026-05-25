package com.ecommerce.order.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * OpenFeign 宣告式呼叫 payment-service
 * 自動整合 Eureka LoadBalancer + Resilience4j
 */
@FeignClient(
    name = "payment-service",
    fallbackFactory = PaymentFeignFallbackFactory.class
)
public interface PaymentFeignClient {

    @PostMapping("/api/payments")
    Map<String, Object> createPayment(@RequestBody Map<String, Object> paymentRequest);

    @GetMapping("/api/payments/order/{orderId}")
    Map<String, Object> getPaymentByOrder(@PathVariable("orderId") Long orderId);

    @GetMapping("/api/payments/health")
    String health();
}

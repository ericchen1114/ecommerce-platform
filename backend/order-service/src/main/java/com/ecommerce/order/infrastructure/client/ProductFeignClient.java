package com.ecommerce.order.infrastructure.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * OpenFeign 宣告式 HTTP Client
 * 透過 Eureka 服務名稱自動解析 URL（LoadBalancer 自動負載均衡）
 * 整合 Resilience4j CircuitBreaker
 */
@FeignClient(
    name = "product-service",        // Eureka 服務名稱（自動解析 IP:port）
    fallbackFactory = ProductFeignFallbackFactory.class
)
public interface ProductFeignClient {

    @GetMapping("/api/products")
    List<Map<String, Object>> getAllProducts();

    @GetMapping("/api/products/{id}")
    Map<String, Object> getProductById(@PathVariable("id") String productId);

    @GetMapping("/api/products/category/{category}")
    List<Map<String, Object>> getByCategory(@PathVariable("category") String category);

    @GetMapping("/api/products/search")
    List<Map<String, Object>> searchProducts(@RequestParam("keyword") String keyword);

    @GetMapping("/api/products/health")
    String health();
}

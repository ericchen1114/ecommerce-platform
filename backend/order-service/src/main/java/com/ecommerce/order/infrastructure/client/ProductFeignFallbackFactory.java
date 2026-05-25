package com.ecommerce.order.infrastructure.client;

import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Feign Fallback Factory
 * 可取得觸發 fallback 的原始 Throwable，方便記錄原因
 */
@Log4j2
@Component
public class ProductFeignFallbackFactory implements FallbackFactory<ProductFeignClient> {

    @Override
    public ProductFeignClient create(Throwable cause) {
        return new ProductFeignClient() {

            @Override
            public List<Map<String, Object>> getAllProducts() {
                log.warn("[FEIGN FALLBACK] product-service getAllProducts 失敗: {}", cause.getMessage());
                return Collections.emptyList();
            }

            @Override
            public Map<String, Object> getProductById(String productId) {
                log.warn("[FEIGN FALLBACK] product-service getProductById={} 失敗: {}", productId, cause.getMessage());
                return Map.of(
                    "id", productId,
                    "name", "商品暫時無法取得",
                    "price", BigDecimal.ZERO,
                    "stock", 0,
                    "available", false
                );
            }

            @Override
            public List<Map<String, Object>> getByCategory(String category) {
                log.warn("[FEIGN FALLBACK] product-service getByCategory={} 失敗", category);
                return Collections.emptyList();
            }

            @Override
            public List<Map<String, Object>> searchProducts(String keyword) {
                log.warn("[FEIGN FALLBACK] product-service searchProducts={} 失敗", keyword);
                return Collections.emptyList();
            }

            @Override
            public String health() {
                return "product-service unavailable";
            }
        };
    }
}

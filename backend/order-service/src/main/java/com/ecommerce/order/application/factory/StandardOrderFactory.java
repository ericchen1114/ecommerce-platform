package com.ecommerce.order.application.factory;

import com.ecommerce.order.domain.exception.BusinessException;
import com.ecommerce.order.domain.factory.OrderFactory;
import com.ecommerce.order.domain.factory.OrderType;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.infrastructure.client.ProductFeignClient;
import com.ecommerce.order.interfaces.dto.CreateOrderRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 標準訂單工廠（Factory Pattern - Concrete Factory）
 *
 * <p>負責建立 {@link OrderType#STANDARD} 類型的訂單，流程：
 * <ol>
 *   <li>透過 {@link ProductFeignClient} 驗證商品是否存在</li>
 *   <li>驗證庫存是否足夠（{@code stock >= quantity}）</li>
 *   <li>建立並回傳初始化的 {@link Order}（狀態 PENDING）</li>
 * </ol>
 * </p>
 *
 * <p><b>注意：</b>此工廠只負責訂單物件的初始化，不扣減庫存。
 * 庫存扣減應在 Saga 流程中由 product-service 處理。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StandardOrderFactory implements OrderFactory {

    private final ProductFeignClient productFeignClient;

    /**
     * 建立標準訂單
     *
     * <p>驗證商品存在性與庫存充足後建立訂單，計算基礎金額（單價 × 數量）。
     * 最終金額由 {@link com.ecommerce.order.application.pricing.PricingContext} 計算。</p>
     *
     * @param req 建立訂單請求（需包含 productId、quantity）
     * @return    已初始化的標準訂單（totalAmount 為基礎金額，待策略調整）
     * @throws BusinessException 商品不存在或庫存不足時拋出
     */
    @Override
    public Order create(CreateOrderRequest req) {
        log.debug("[StandardOrderFactory] 建立標準訂單，productId={}, quantity={}",
                req.getProductId(), req.getQuantity());

        // 驗證商品是否存在
        Map<String, Object> product = productFeignClient.getProductById(
                String.valueOf(req.getProductId()));
        if (product == null || product.isEmpty()) {
            throw new BusinessException("商品不存在：productId=" + req.getProductId());
        }

        // 驗證庫存
        Object stockObj = product.get("stock");
        int stock = stockObj != null ? Integer.parseInt(stockObj.toString()) : 0;
        if (stock < req.getQuantity()) {
            throw new BusinessException(
                    String.format("庫存不足，當前庫存：%d，請求數量：%d", stock, req.getQuantity()));
        }

        // 計算基礎金額（最終金額由 PricingContext 處理）
        Object priceObj = product.get("price");
        BigDecimal unitPrice = priceObj != null
                ? new BigDecimal(priceObj.toString())
                : BigDecimal.ZERO;
        BigDecimal baseAmount = unitPrice.multiply(BigDecimal.valueOf(req.getQuantity()));

        log.debug("[StandardOrderFactory] 基礎金額計算完成：unitPrice={}, quantity={}, baseAmount={}",
                unitPrice, req.getQuantity(), baseAmount);

        return Order.builder()
                .userId(req.getUserId())
                .orderType(OrderType.STANDARD)
                .totalAmount(baseAmount)
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(req.getShippingAddress())
                .region(req.getRegion())
                .memberLevel(req.getMemberLevel())
                .member(req.isMember())
                .promoCode(req.getPromoCode())
                .shippingState(req.getShippingState())
                .note(req.getNote())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * 判斷此工廠是否支援指定訂單類型
     *
     * @param type 訂單類型
     * @return     {@code true} 僅當 type 為 {@link OrderType#STANDARD}
     */
    @Override
    public boolean supports(OrderType type) {
        return OrderType.STANDARD.equals(type);
    }
}

package com.ecommerce.order.application.pricing;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.service.PricingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * 計價上下文（Strategy Pattern - Context）
 *
 * <p>組合並依序執行所有 {@link PricingStrategy}，
 * 將基礎金額依固定順序逐步調整為最終訂單金額。</p>
 *
 * <h3>策略執行順序（影響最終金額，不可隨意調換）</h3>
 * <ol>
 *   <li>{@link MemberDiscountStrategy}  - 會員折扣（先打折再算其他費用）</li>
 *   <li>{@link PromotionStrategy}       - 促銷碼折扣（折上折）</li>
 *   <li>{@link RegionalPricingStrategy} - 地區運費（加收地區附加費）</li>
 *   <li>{@link TaxStrategy}             - 稅率（以實付金額為基礎計稅）</li>
 * </ol>
 *
 * <h3>擴充說明</h3>
 * <p>新增計價規則只需：
 * <ol>
 *   <li>實作 {@link PricingStrategy} 並標注 {@code @Component}</li>
 *   <li>在 {@link #ORDERED_STRATEGY_TYPES} 中加入執行順序</li>
 * </ol>
 * </p>
 */
@Slf4j
@Component
public class PricingContext {

    /**
     * 策略執行順序（類型列表），決定各策略套用的先後順序
     *
     * <p>使用類型匹配而非直接注入列表，確保執行順序不受 Spring Bean 注入順序影響。</p>
     */
    private static final List<Class<? extends PricingStrategy>> ORDERED_STRATEGY_TYPES = List.of(
            MemberDiscountStrategy.class,
            PromotionStrategy.class,
            RegionalPricingStrategy.class,
            TaxStrategy.class
    );

    /** 所有已注入的計價策略（由 Spring 自動注入） */
    private final List<PricingStrategy> strategies;

    /**
     * @param strategies Spring 自動注入的所有 {@link PricingStrategy} Bean
     */
    public PricingContext(List<PricingStrategy> strategies) {
        this.strategies = strategies;
    }

    /**
     * 依序套用所有計價策略，計算最終訂單金額
     *
     * <p>執行流程：
     * <pre>
     * basePrice
     *   → [會員折扣] → 中間金額1
     *   → [促銷碼]  → 中間金額2
     *   → [地區運費] → 中間金額3
     *   → [稅率]    → 最終金額（四捨五入至小數點後 2 位）
     * </pre>
     * </p>
     *
     * @param basePrice 商品基礎金額（單價 × 數量）
     * @param order     訂單領域物件（提供地區、會員等級、促銷碼等計算依據）
     * @return          套用所有策略後的最終金額（四捨五入至小數點後 2 位）
     */
    public BigDecimal calculateFinalPrice(BigDecimal basePrice, Order order) {
        log.debug("[PricingContext] 開始計算最終金額，basePrice={}", basePrice);

        BigDecimal result = basePrice;

        for (Class<? extends PricingStrategy> strategyType : ORDERED_STRATEGY_TYPES) {
            PricingStrategy strategy = findStrategy(strategyType);
            if (strategy == null) continue;

            BigDecimal before = result;
            result = strategy.apply(result, order);
            log.debug("[PricingContext] {} 策略執行：{} → {}", strategy.getName(), before, result);
        }

        // 四捨五入至小數點後 2 位
        BigDecimal finalPrice = result.setScale(2, RoundingMode.HALF_UP);
        log.info("[PricingContext] 最終金額計算完成：basePrice={} → finalPrice={}", basePrice, finalPrice);
        return finalPrice;
    }

    /**
     * 依類型從已注入的策略列表中查找對應策略
     *
     * @param strategyType 策略類型
     * @return             對應的策略實作，若未找到則回傳 {@code null}
     */
    private PricingStrategy findStrategy(Class<? extends PricingStrategy> strategyType) {
        return strategies.stream()
                .filter(s -> strategyType.isAssignableFrom(s.getClass()))
                .findFirst()
                .orElse(null);
    }
}

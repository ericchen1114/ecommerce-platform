package com.ecommerce.order.application.pricing;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.service.PricingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 會員折扣策略（Strategy Pattern - Concrete Strategy）
 *
 * <p>根據會員等級套用對應折扣率：
 * <ul>
 *   <li>{@code GOLD}   - 85 折（× 0.85）</li>
 *   <li>{@code SILVER} - 90 折（× 0.90）</li>
 *   <li>{@code BRONZE} - 95 折（× 0.95）</li>
 *   <li>非會員 / 其他等級 - 無折扣（× 1.0）</li>
 * </ul>
 * </p>
 *
 * <p><b>執行順序：第一順位</b>（在促銷碼之前先扣會員折扣）</p>
 */
@Slf4j
@Component
public class MemberDiscountStrategy implements PricingStrategy {

    /** 各會員等級對應折扣率 */
    private static final Map<String, BigDecimal> DISCOUNT_RATES = Map.of(
            "GOLD",   new BigDecimal("0.85"),
            "SILVER", new BigDecimal("0.90"),
            "BRONZE", new BigDecimal("0.95")
    );

    /**
     * 套用會員折扣
     *
     * <p>若訂單標記為會員（{@code order.isMember() == true}），
     * 依 {@code order.getMemberLevel()} 查詢折扣率並計算；
     * 若非會員或等級不在表內，直接回傳原金額。</p>
     *
     * @param basePrice 套用前金額
     * @param order     訂單（讀取 member、memberLevel 欄位）
     * @return          套用會員折扣後的金額
     */
    @Override
    public BigDecimal apply(BigDecimal basePrice, Order order) {
        if (!order.isMember()) {
            return basePrice;
        }

        BigDecimal rate = DISCOUNT_RATES.getOrDefault(
                order.getMemberLevel(), BigDecimal.ONE);

        BigDecimal result = basePrice.multiply(rate);
        log.debug("[{}] memberLevel={}, rate={}, {} → {}",
                getName(), order.getMemberLevel(), rate, basePrice, result);
        return result;
    }

    /**
     * @return 策略名稱（用於日誌）
     */
    @Override
    public String getName() {
        return "會員折扣";
    }
}

package com.ecommerce.order.application.pricing;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.service.PricingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 稅率計算策略（Strategy Pattern - Concrete Strategy）
 *
 * <p>依訂單的寄送州/地區（{@code shippingState}）套用對應稅率：
 * <ul>
 *   <li>{@code CA} - 加州稅 8.5%（× 1.085）</li>
 *   <li>{@code NY} - 紐約州稅 8.875%（× 1.08875）</li>
 *   <li>{@code TX} - 德州稅 6.25%（× 1.0625）</li>
 *   <li>未設定或其他州 - 無稅（× 1.0）</li>
 * </ul>
 * </p>
 *
 * <p><b>執行順序：第四順位（最後）</b>
 * 稅率應以折扣後的實付金額為基礎計算，因此排最後執行。</p>
 */
@Slf4j
@Component
public class TaxStrategy implements PricingStrategy {

    /** 各州稅率倍率（1.0 = 無稅，1.085 = 8.5% 稅率） */
    private static final Map<String, BigDecimal> TAX_RATES = Map.of(
            "CA", new BigDecimal("1.085"),
            "NY", new BigDecimal("1.08875"),
            "TX", new BigDecimal("1.0625"),
            "DEFAULT", BigDecimal.ONE
    );

    /**
     * 套用稅率
     *
     * <p>依 {@code order.getShippingState()} 查詢稅率倍率，
     * 未設定或不在表內則不加稅（× 1.0）。</p>
     *
     * @param basePrice 套用前金額（折扣後的實付金額）
     * @param order     訂單（讀取 shippingState 欄位）
     * @return          含稅後的最終金額
     */
    @Override
    public BigDecimal apply(BigDecimal basePrice, Order order) {
        String state = order.getShippingState();
        BigDecimal taxRate = (state != null)
                ? TAX_RATES.getOrDefault(state.toUpperCase(), TAX_RATES.get("DEFAULT"))
                : TAX_RATES.get("DEFAULT");

        BigDecimal result = basePrice.multiply(taxRate);
        log.debug("[{}] shippingState={}, taxRate={}, {} → {}",
                getName(), state, taxRate, basePrice, result);
        return result;
    }

    /**
     * @return 策略名稱（用於日誌）
     */
    @Override
    public String getName() {
        return "稅率";
    }
}

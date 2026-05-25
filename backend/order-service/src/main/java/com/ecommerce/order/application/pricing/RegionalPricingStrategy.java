package com.ecommerce.order.application.pricing;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.service.PricingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 地區運費策略（Strategy Pattern - Concrete Strategy）
 *
 * <p>依訂單的地區代碼（{@code region}）套用不同的運費倍率：
 * <ul>
 *   <li>{@code NYC}     - 加收 10%（× 1.10）</li>
 *   <li>{@code LA}      - 加收 5%（× 1.05）</li>
 *   <li>{@code MIDWEST} - 加收 2%（× 1.02）</li>
 *   <li>未設定或其他地區 - 不加收（× 1.0）</li>
 * </ul>
 * </p>
 *
 * <p><b>執行順序：第三順位</b>（在促銷碼之後、稅率之前）</p>
 *
 * <p><b>擴充說明：</b>若需支援更多地區，直接擴充 {@code RATES} Map，
 * 無需修改其他任何類別。</p>
 */
@Slf4j
@Component
public class RegionalPricingStrategy implements PricingStrategy {

    /** 各地區對應運費倍率（1.0 = 無加收，1.10 = 加收 10%） */
    private static final Map<String, BigDecimal> RATES = Map.of(
            "NYC",     new BigDecimal("1.10"),
            "LA",      new BigDecimal("1.05"),
            "MIDWEST", new BigDecimal("1.02"),
            "DEFAULT", new BigDecimal("1.0")
    );

    /**
     * 套用地區運費倍率
     *
     * <p>依 {@code order.getRegion()} 查詢倍率，未設定地區則使用 DEFAULT（1.0）。</p>
     *
     * @param basePrice 套用前金額
     * @param order     訂單（讀取 region 欄位）
     * @return          加上地區運費後的金額
     */
    @Override
    public BigDecimal apply(BigDecimal basePrice, Order order) {
        String region = order.getRegion();
        BigDecimal rate = (region != null)
                ? RATES.getOrDefault(region.toUpperCase(), RATES.get("DEFAULT"))
                : RATES.get("DEFAULT");

        BigDecimal result = basePrice.multiply(rate);
        log.debug("[{}] region={}, rate={}, {} → {}",
                getName(), region, rate, basePrice, result);
        return result;
    }

    /**
     * @return 策略名稱（用於日誌）
     */
    @Override
    public String getName() {
        return "地區運費";
    }
}

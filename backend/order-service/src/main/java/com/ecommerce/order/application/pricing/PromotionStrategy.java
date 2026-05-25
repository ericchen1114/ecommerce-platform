package com.ecommerce.order.application.pricing;

import com.ecommerce.order.domain.exception.BusinessException;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.service.PricingStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 促銷碼折扣策略（Strategy Pattern - Concrete Strategy）
 *
 * <p>根據訂單中的促銷碼（{@code promoCode}）套用折扣，支援兩種折扣型態：
 * <ul>
 *   <li>{@code PERCENTAGE} - 百分比折扣，如 9 折（discountValue = 0.10，即減少 10%）</li>
 *   <li>{@code FIXED}      - 固定金額折扣，如減 100 元</li>
 * </ul>
 * </p>
 *
 * <p><b>執行順序：第二順位</b>（在會員折扣之後、地區運費之前）</p>
 *
 * <p><b>說明：</b>目前促銷碼規則以 in-memory Map 模擬，
 * 實際專案應替換為 PromotionRepository 查詢資料庫。</p>
 */
@Slf4j
@Component
public class PromotionStrategy implements PricingStrategy {

    /**
     * 促銷碼定義（模擬資料，實際應由 DB 查詢）
     *
     * <p>Key: 促銷碼字串<br>
     * Value: [0] = 折扣型態（PERCENTAGE/FIXED）、[1] = 折扣值、[2] = 是否有效</p>
     */
    private static final Map<String, Object[]> PROMOS = new HashMap<>();

    static {
        // SUMMER10：百分比折扣 10%（即 9 折），有效
        PROMOS.put("SUMMER10", new Object[]{"PERCENTAGE", new BigDecimal("0.10"), true});
        // SAVE100：固定折扣 100 元，有效
        PROMOS.put("SAVE100",  new Object[]{"FIXED",      new BigDecimal("100"),  true});
        // EXPIRED：已過期的促銷碼
        PROMOS.put("EXPIRED",  new Object[]{"PERCENTAGE", new BigDecimal("0.20"), false});
    }

    /**
     * 套用促銷碼折扣
     *
     * <p>若訂單無促銷碼則跳過；有促銷碼時驗證有效性，
     * 依折扣型態計算最終金額（金額最小為 0，不可為負）。</p>
     *
     * @param basePrice 套用前金額
     * @param order     訂單（讀取 promoCode 欄位）
     * @return          套用促銷碼後的金額
     * @throws BusinessException 促銷碼無效或已過期時拋出
     */
    @Override
    public BigDecimal apply(BigDecimal basePrice, Order order) {
        String code = order.getPromoCode();
        if (code == null || code.isBlank()) {
            return basePrice;
        }

        Object[] promo = PROMOS.get(code.toUpperCase());
        if (promo == null) {
            throw new BusinessException("無效的促銷碼：" + code);
        }

        boolean active = (boolean) promo[2];
        if (!active) {
            throw new BusinessException("促銷碼已過期：" + code);
        }

        String discountType    = (String)     promo[0];
        BigDecimal discountVal = (BigDecimal) promo[1];

        BigDecimal result;
        if ("PERCENTAGE".equals(discountType)) {
            result = basePrice.multiply(BigDecimal.ONE.subtract(discountVal));
        } else {
            result = basePrice.subtract(discountVal);
        }

        // 金額不可為負
        result = result.max(BigDecimal.ZERO);

        log.debug("[{}] promoCode={}, type={}, discountVal={}, {} → {}",
                getName(), code, discountType, discountVal, basePrice, result);
        return result;
    }

    /**
     * @return 策略名稱（用於日誌）
     */
    @Override
    public String getName() {
        return "促銷碼折扣";
    }
}

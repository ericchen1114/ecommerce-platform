package com.ecommerce.order.application.pricing;

import com.ecommerce.order.domain.exception.BusinessException;
import com.ecommerce.order.domain.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 計價策略單元測試
 *
 * <p>分別驗證 MemberDiscountStrategy、PromotionStrategy、
 * RegionalPricingStrategy、TaxStrategy 各自的計算邏輯。</p>
 */
@DisplayName("計價策略單元測試")
class PricingStrategyTest {

    // ─── 輔助方法：建立測試用 Order ──────────────────────────────────────────

    /**
     * 建立測試用訂單（使用 Builder 直接設定各策略所需欄位）
     */
    private Order buildOrder(String region, String memberLevel,
                              boolean member, String promoCode, String shippingState) {
        return Order.builder()
                .userId(1L)
                .region(region)
                .memberLevel(memberLevel)
                .member(member)
                .promoCode(promoCode)
                .shippingState(shippingState)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();
    }

    // ─── MemberDiscountStrategy 測試 ─────────────────────────────────────────

    @Test
    @DisplayName("GOLD 會員 → 85 折")
    void memberDiscount_Gold() {
        MemberDiscountStrategy strategy = new MemberDiscountStrategy();
        Order order = buildOrder(null, "GOLD", true, null, null);

        BigDecimal result = strategy.apply(new BigDecimal("1000"), order);

        assertThat(result).isEqualByComparingTo("850.00");
    }

    @Test
    @DisplayName("SILVER 會員 → 90 折")
    void memberDiscount_Silver() {
        MemberDiscountStrategy strategy = new MemberDiscountStrategy();
        Order order = buildOrder(null, "SILVER", true, null, null);

        BigDecimal result = strategy.apply(new BigDecimal("1000"), order);

        assertThat(result).isEqualByComparingTo("900.00");
    }

    @Test
    @DisplayName("非會員 → 無折扣")
    void memberDiscount_NonMember() {
        MemberDiscountStrategy strategy = new MemberDiscountStrategy();
        Order order = buildOrder(null, null, false, null, null);

        BigDecimal result = strategy.apply(new BigDecimal("1000"), order);

        assertThat(result).isEqualByComparingTo("1000");
    }

    // ─── PromotionStrategy 測試 ──────────────────────────────────────────────

    @Test
    @DisplayName("SUMMER10 促銷碼 → 打 9 折")
    void promotion_Summer10() {
        PromotionStrategy strategy = new PromotionStrategy();
        Order order = buildOrder(null, null, false, "SUMMER10", null);

        BigDecimal result = strategy.apply(new BigDecimal("1000"), order);

        assertThat(result).isEqualByComparingTo("900.00");
    }

    @Test
    @DisplayName("SAVE100 促銷碼 → 固定折扣 100 元")
    void promotion_Save100() {
        PromotionStrategy strategy = new PromotionStrategy();
        Order order = buildOrder(null, null, false, "SAVE100", null);

        BigDecimal result = strategy.apply(new BigDecimal("300"), order);

        assertThat(result).isEqualByComparingTo("200.00");
    }

    @Test
    @DisplayName("無促銷碼 → 不變")
    void promotion_NoCode() {
        PromotionStrategy strategy = new PromotionStrategy();
        Order order = buildOrder(null, null, false, null, null);

        BigDecimal result = strategy.apply(new BigDecimal("500"), order);

        assertThat(result).isEqualByComparingTo("500");
    }

    @Test
    @DisplayName("過期促銷碼 → 拋出 BusinessException")
    void promotion_Expired_ThrowsException() {
        PromotionStrategy strategy = new PromotionStrategy();
        Order order = buildOrder(null, null, false, "EXPIRED", null);

        assertThatThrownBy(() -> strategy.apply(new BigDecimal("500"), order))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("促銷碼已過期");
    }

    @Test
    @DisplayName("無效促銷碼 → 拋出 BusinessException")
    void promotion_Invalid_ThrowsException() {
        PromotionStrategy strategy = new PromotionStrategy();
        Order order = buildOrder(null, null, false, "INVALID_CODE", null);

        assertThatThrownBy(() -> strategy.apply(new BigDecimal("500"), order))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("無效的促銷碼");
    }

    // ─── RegionalPricingStrategy 測試 ────────────────────────────────────────

    @Test
    @DisplayName("NYC 地區 → 加收 10%")
    void regional_NYC() {
        RegionalPricingStrategy strategy = new RegionalPricingStrategy();
        Order order = buildOrder("NYC", null, false, null, null);

        BigDecimal result = strategy.apply(new BigDecimal("1000"), order);

        assertThat(result).isEqualByComparingTo("1100.00");
    }

    @Test
    @DisplayName("無地區設定 → 不加收")
    void regional_NoRegion() {
        RegionalPricingStrategy strategy = new RegionalPricingStrategy();
        Order order = buildOrder(null, null, false, null, null);

        BigDecimal result = strategy.apply(new BigDecimal("1000"), order);

        assertThat(result).isEqualByComparingTo("1000.00");
    }

    // ─── TaxStrategy 測試 ────────────────────────────────────────────────────

    @Test
    @DisplayName("CA（加州）→ 加 8.5% 稅")
    void tax_California() {
        TaxStrategy strategy = new TaxStrategy();
        Order order = buildOrder(null, null, false, null, "CA");

        BigDecimal result = strategy.apply(new BigDecimal("1000"), order);

        assertThat(result).isEqualByComparingTo("1085.00");
    }

    @Test
    @DisplayName("無州別設定 → 不加稅")
    void tax_NoState() {
        TaxStrategy strategy = new TaxStrategy();
        Order order = buildOrder(null, null, false, null, null);

        BigDecimal result = strategy.apply(new BigDecimal("1000"), order);

        assertThat(result).isEqualByComparingTo("1000.00");
    }
}

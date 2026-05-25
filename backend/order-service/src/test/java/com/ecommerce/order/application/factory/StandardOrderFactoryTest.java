package com.ecommerce.order.application.factory;

import com.ecommerce.order.domain.exception.BusinessException;
import com.ecommerce.order.domain.factory.OrderType;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.infrastructure.client.ProductFeignClient;
import com.ecommerce.order.interfaces.dto.CreateOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * StandardOrderFactory 單元測試
 *
 * <p>驗證標準訂單工廠的業務規則：
 * <ul>
 *   <li>商品存在且庫存充足 → 成功建立訂單</li>
 *   <li>商品不存在 → 拋出 BusinessException</li>
 *   <li>庫存不足 → 拋出 BusinessException</li>
 *   <li>{@code supports()} 僅回傳 STANDARD 類型為 true</li>
 * </ul>
 * </p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StandardOrderFactory 單元測試")
class StandardOrderFactoryTest {

    @InjectMocks
    private StandardOrderFactory factory;

    @Mock
    private ProductFeignClient productFeignClient;

    private CreateOrderRequest baseRequest;

    /**
     * 每個測試前準備基礎請求物件
     */
    @BeforeEach
    void setUp() {
        baseRequest = new CreateOrderRequest();
        baseRequest.setOrderType(OrderType.STANDARD);
        baseRequest.setUserId(1L);
        baseRequest.setProductId(1L);
        baseRequest.setQuantity(5);
        baseRequest.setShippingAddress("台北市信義區");
    }

    /**
     * 測試正常建立標準訂單
     *
     * <p>當商品存在且庫存充足時，應正確建立訂單並計算基礎金額。</p>
     */
    @Test
    @DisplayName("商品存在且庫存充足 → 成功建立 STANDARD 訂單")
    void create_Success() {
        // Arrange：Mock 商品資料（price=100, stock=100）
        Map<String, Object> product = Map.of(
                "id",    1,
                "price", "100",
                "stock", "100"
        );
        when(productFeignClient.getProductById("1")).thenReturn(product);

        // Act
        Order order = factory.create(baseRequest);

        // Assert
        assertThat(order.getOrderType()).isEqualTo(OrderType.STANDARD);
        assertThat(order.getUserId()).isEqualTo(1L);
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        // 基礎金額 = 100 × 5 = 500
        assertThat(order.getTotalAmount()).isEqualByComparingTo("500");
        assertThat(order.getShippingAddress()).isEqualTo("台北市信義區");
    }

    /**
     * 測試商品不存在時拋出例外
     *
     * <p>當 ProductFeignClient 回傳 null 時，應拋出 BusinessException。</p>
     */
    @Test
    @DisplayName("商品不存在 → 拋出 BusinessException")
    void create_ProductNotFound_ThrowsException() {
        // Arrange：Mock 商品不存在（回傳 null）
        when(productFeignClient.getProductById("1")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> factory.create(baseRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("商品不存在");
    }

    /**
     * 測試商品不存在時拋出例外（回傳空 Map）
     */
    @Test
    @DisplayName("商品查詢回傳空 Map → 拋出 BusinessException")
    void create_ProductEmpty_ThrowsException() {
        // Arrange：Mock 商品回傳空 Map
        when(productFeignClient.getProductById("1")).thenReturn(Map.of());

        // Act & Assert
        assertThatThrownBy(() -> factory.create(baseRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("商品不存在");
    }

    /**
     * 測試庫存不足時拋出例外
     *
     * <p>當商品存在但庫存（3）小於請求數量（5）時，應拋出 BusinessException。</p>
     */
    @Test
    @DisplayName("庫存不足 → 拋出 BusinessException")
    void create_InsufficientStock_ThrowsException() {
        // Arrange：Mock 庫存只有 3，請求數量 5
        Map<String, Object> product = Map.of(
                "id",    1,
                "price", "100",
                "stock", "3"      // 庫存不足
        );
        when(productFeignClient.getProductById("1")).thenReturn(product);

        // Act & Assert
        assertThatThrownBy(() -> factory.create(baseRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("庫存不足");
    }

    /**
     * 測試 supports() 對 STANDARD 回傳 true
     */
    @Test
    @DisplayName("supports(STANDARD) → true")
    void supports_StandardType_ReturnsTrue() {
        assertThat(factory.supports(OrderType.STANDARD)).isTrue();
    }

    /**
     * 測試 supports() 對非 STANDARD 類型回傳 false
     */
    @Test
    @DisplayName("supports(GIFT_CARD) → false")
    void supports_OtherType_ReturnsFalse() {
        assertThat(factory.supports(OrderType.GIFT_CARD)).isFalse();
        assertThat(factory.supports(OrderType.SUBSCRIPTION)).isFalse();
    }

    /**
     * 測試訂單數量為 1 時的邊界情況
     */
    @Test
    @DisplayName("數量為 1 且庫存恰好為 1 → 成功建立（邊界情況）")
    void create_QuantityEqualsStock_Success() {
        // Arrange：庫存 = 數量 = 1
        baseRequest.setQuantity(1);
        Map<String, Object> product = Map.of(
                "id",    1,
                "price", "250",
                "stock", "1"
        );
        when(productFeignClient.getProductById("1")).thenReturn(product);

        // Act
        Order order = factory.create(baseRequest);

        // Assert：基礎金額 = 250 × 1 = 250
        assertThat(order.getTotalAmount()).isEqualByComparingTo("250");
    }
}

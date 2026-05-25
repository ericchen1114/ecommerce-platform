package com.ecommerce.order.domain;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderItem;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

class OrderDomainTest {

    @Test
    void 建立訂單_應自動計算總金額() {
        List<OrderItem> items = List.of(
            OrderItem.builder().productId("p1").productName("商品A").quantity(2).price(new BigDecimal("100")).build(),
            OrderItem.builder().productId("p2").productName("商品B").quantity(1).price(new BigDecimal("200")).build()
        );
        Order order = Order.create(1L, "台北市", null, items);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("400");
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
    }

    @Test
    void 確認訂單_狀態應改為CONFIRMED() {
        Order order = Order.create(1L, "台北市", null, List.of(
            OrderItem.builder().productId("p1").productName("A").quantity(1).price(BigDecimal.TEN).build()
        ));
        order.confirm();
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CONFIRMED);
    }

    @Test
    void 非PENDING訂單確認_應拋出例外() {
        Order order = Order.create(1L, "台北市", null, List.of(
            OrderItem.builder().productId("p1").productName("A").quantity(1).price(BigDecimal.TEN).build()
        ));
        order.confirm();
        assertThatThrownBy(order::confirm)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("待處理");
    }
}

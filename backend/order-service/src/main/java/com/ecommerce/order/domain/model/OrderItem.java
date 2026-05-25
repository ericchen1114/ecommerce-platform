package com.ecommerce.order.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@Builder
public class OrderItem {
    private Long id;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}

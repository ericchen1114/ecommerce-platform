package com.ecommerce.order.interfaces.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderSummary {
    private Long orderId;
    private Long userId;
    private String username;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
}

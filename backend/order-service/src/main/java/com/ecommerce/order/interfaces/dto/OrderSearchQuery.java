package com.ecommerce.order.interfaces.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class OrderSearchQuery {
    private Long userId;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int page = 0;
    private int size = 20;
}

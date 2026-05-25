package com.ecommerce.order.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "order_items")
public class OrderItemJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne @JoinColumn(name = "order_id")
    private OrderJpaEntity order;
    private String productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}

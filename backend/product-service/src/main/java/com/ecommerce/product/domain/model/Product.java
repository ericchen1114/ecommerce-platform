package com.ecommerce.product.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class Product {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private String imageUrl;
    private List<String> images;
    private boolean active;
    private LocalDateTime createdAt;

    public static Product create(String name, String description, BigDecimal price,
                                  Integer stock, String category, String imageUrl) {
        return Product.builder()
            .name(name).description(description).price(price)
            .stock(stock).category(category).imageUrl(imageUrl)
            .active(true).createdAt(LocalDateTime.now()).build();
    }

    public boolean hasEnoughStock(int quantity) {
        return this.stock >= quantity;
    }
}

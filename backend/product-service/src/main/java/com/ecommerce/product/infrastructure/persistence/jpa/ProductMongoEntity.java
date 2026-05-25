package com.ecommerce.product.infrastructure.persistence.jpa;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品 MongoDB Document Entity（Infrastructure Layer）
 *
 * <p>對應 MongoDB {@code products} collection，負責 ODM 映射。
 * 不包含任何業務邏輯，轉換邏輯由
 * {@link com.ecommerce.product.infrastructure.persistence.mapper.ProductEntityMapper} 處理（MapStruct 自動生成）。</p>
 *
 * <h3>欄位說明</h3>
 * <ul>
 *   <li>{@code id}        — MongoDB ObjectId（String）</li>
 *   <li>{@code imageUrl}  — 主圖 URL</li>
 *   <li>{@code images}    — 多圖 URL 列表</li>
 *   <li>{@code active}    — 是否上架</li>
 * </ul>
 */
@Data
@Document(collection = "products")
public class ProductMongoEntity {

    @Id
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
}

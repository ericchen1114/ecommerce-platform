package com.ecommerce.product.infrastructure.persistence.jpa;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.TextScore;

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
 *   <li>{@code id}          — MongoDB ObjectId（String）</li>
 *   <li>{@code name}        — 商品名稱（加入 $text 索引，weight=10）</li>
 *   <li>{@code description} — 商品描述（加入 $text 索引，weight=5）</li>
 *   <li>{@code imageUrl}    — 主圖 URL</li>
 *   <li>{@code images}      — 多圖 URL 列表（MongoDB 原生陣列）</li>
 *   <li>{@code active}      — 是否上架</li>
 *   <li>{@code score}       — $text 搜尋相關度分數（僅搜尋時填充）</li>
 * </ul>
 */
@Data
@Document(collection = "products")
@CompoundIndex(name = "category_active_idx", def = "{'category': 1, 'active': 1}")
public class ProductMongoEntity {

    @Id
    private String id;

    /** 商品名稱，納入 $text 全文索引（weight=10，比 description 權重高）*/
    @TextIndexed(weight = 10)
    private String name;

    /** 商品描述，納入 $text 全文索引（weight=5）*/
    @TextIndexed(weight = 5)
    private String description;

    private BigDecimal price;
    private Integer stock;
    private String category;
    private String imageUrl;

    /** 多圖陣列（MongoDB 原生支援，無需中間表）*/
    private List<String> images;

    private boolean active;
    private LocalDateTime createdAt;

    /** $text 搜尋相關度分數，僅 searchByText 查詢時填充 */
    @TextScore
    private Float score;
}

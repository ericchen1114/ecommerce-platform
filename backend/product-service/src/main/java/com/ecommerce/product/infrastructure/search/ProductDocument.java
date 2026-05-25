package com.ecommerce.product.infrastructure.search;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 商品文件
 * Index：ecommerce-products
 *
 * 功能：
 *   - 商品全文搜尋（名稱、描述）
 *   - 規格篩選（specifications JSONB 內容同步過來）
 *   - 標籤搜尋
 *   - 價格範圍查詢
 */
@Document(indexName = "ecommerce-products")
@Setting(settingPath = "/elasticsearch/product-settings.json")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDocument {

    @Id
    private String id;          // product.id 轉 String

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Double)
    private BigDecimal price;

    @Field(type = FieldType.Integer)
    private Integer stock;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    // 商品規格（從 JSONB 同步過來，供篩選用）
    @Field(type = FieldType.Object)
    private Map<String, Object> specifications;

    // 標籤（陣列，供 terms 查詢）
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Boolean)
    private Boolean active;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;
}

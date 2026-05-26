package com.ecommerce.product.infrastructure.config;

import com.ecommerce.product.infrastructure.persistence.jpa.ProductMongoEntity;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

/**
 * MongoDB 索引設定
 *
 * <p>應用程式啟動後自動確保以下索引存在（冪等，已存在不重建）：</p>
 * <ul>
 *   <li><b>$text index</b>：name（weight=10）+ description（weight=5），供全文搜尋使用</li>
 *   <li><b>category + active 複合索引</b>：加速分類篩選查詢</li>
 * </ul>
 *
 * <h3>為何選 MongoDB $text 而非 Elasticsearch</h3>
 * <p>商品量級在百萬筆以內時，$text 索引效能完全足夠；
 * Elasticsearch 每節點需 4–8 GB JVM Heap，與 MongoDB 之間的同步鏈一旦中斷
 * 會導致搜尋結果與實際資料不一致，維運風險超出現階段團隊規模。</p>
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MongoConfig {

    private final MongoTemplate mongoTemplate;

    /**
     * 啟動時確保 products collection 的索引已建立
     */
    @PostConstruct
    public void ensureIndexes() {
        IndexOperations indexOps = mongoTemplate.indexOps(ProductMongoEntity.class);

        // ① $text 全文索引：name(weight=10) + description(weight=5)
        TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("name", 10F)
                .onField("description", 5F)
                .build();
        indexOps.ensureIndex(textIndex);
        log.info("[MongoConfig] $text index 已確保（name w=10, description w=5）");

        // ② category + active 複合索引（分類頁常用查詢）
        indexOps.ensureIndex(
                new Index().on("category", Sort.Direction.ASC)
                           .on("active", Sort.Direction.ASC)
                           .named("category_active_idx")
        );
        log.info("[MongoConfig] category_active_idx 已確保");
    }
}

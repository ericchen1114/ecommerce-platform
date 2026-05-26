package com.ecommerce.product.infrastructure.persistence.jpa;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * 商品 MongoDB Repository
 *
 * <h3>搜尋策略</h3>
 * <ul>
 *   <li>分類/上架狀態查詢：Spring Data 衍生查詢（自動生成）</li>
 *   <li>關鍵字全文搜尋：MongoDB {@code $text} 索引，依相關度分數排序</li>
 * </ul>
 *
 * <p>{@code $text} 索引由 {@link com.ecommerce.product.infrastructure.config.MongoConfig}
 * 在啟動時自動建立，涵蓋 {@code name}（weight=10）與 {@code description}（weight=5）。</p>
 */
public interface ProductMongoRepository extends MongoRepository<ProductMongoEntity, String> {

    /** 查詢所有上架商品 */
    List<ProductMongoEntity> findByActiveTrue();

    /** 依分類查詢上架商品 */
    List<ProductMongoEntity> findByCategoryAndActiveTrue(String category);

    /**
     * $text 全文搜尋：依 TextScore 相關度降冪排序
     *
     * <p>使用 {@code $text: {$search: keyword}} 查詢，
     * 結合 {@code {score: {$meta: "textScore"}}} 排序，
     * 確保最相關的商品優先回傳。</p>
     *
     * @param keyword 搜尋關鍵字（支援空格分詞、"-詞" 排除）
     * @param sort    排序（需傳入 {@code Sort.by("score")} 以啟用 textScore 排序）
     * @return        符合關鍵字且上架的商品列表
     */
    @Query("{ $text: { $search: ?0 }, active: true }")
    List<ProductMongoEntity> findByTextAndActiveTrue(String keyword, Sort sort);
}

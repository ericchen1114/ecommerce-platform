package com.ecommerce.product.infrastructure.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductDocument, String> {

    // 關鍵字搜尋（名稱 + 描述）
    Page<ProductDocument> findByNameContainingOrDescriptionContaining(
            String name, String desc, Pageable pageable);

    // 品類篩選
    Page<ProductDocument> findByCategoryAndActiveTrue(String category, Pageable pageable);

    // 價格區間
    Page<ProductDocument> findByPriceBetweenAndActiveTrue(
            BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // 標籤搜尋
    Page<ProductDocument> findByTagsContaining(String tag, Pageable pageable);

    // 複合搜尋（關鍵字 + 品類 + 價格範圍）
    @Query("""
        {
          "bool": {
            "must": [
              {"multi_match": {
                "query": "?0",
                "fields": ["name^2", "description"]
              }}
            ],
            "filter": [
              {"term":  {"active": true}},
              {"term":  {"category": "?1"}},
              {"range": {"price": {"gte": ?2, "lte": ?3}}}
            ]
          }
        }
        """)
    Page<ProductDocument> searchProducts(
            String keyword, String category,
            BigDecimal minPrice, BigDecimal maxPrice,
            Pageable pageable);
}

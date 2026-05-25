package com.ecommerce.order.infrastructure.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface OrderSearchRepository
        extends ElasticsearchRepository<OrderDocument, String> {

    // 查某用戶所有訂單
    List<OrderDocument> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 按狀態查詢
    Page<OrderDocument> findByStatus(String status, Pageable pageable);

    // 商品名稱關鍵字查訂單（Nested 查詢）
    @Query("""
        {
          "nested": {
            "path": "items",
            "query": {
              "match": { "items.productName": "?0" }
            }
          }
        }
        """)
    Page<OrderDocument> findByProductName(String productName, Pageable pageable);

    // 查含某品類商品的訂單（後台報表用）
    @Query("""
        {
          "nested": {
            "path": "items",
            "query": {
              "term": { "items.category": "?0" }
            }
          }
        }
        """)
    Page<OrderDocument> findByProductCategory(String category, Pageable pageable);
}

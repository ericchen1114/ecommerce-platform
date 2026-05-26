package com.ecommerce.product.infrastructure.search;

/**
 * @deprecated 商品搜尋已改用 MongoDB $text 原生索引，此類已廢棄。
 * 請使用 {@link com.ecommerce.product.infrastructure.persistence.jpa.ProductRepositoryImpl#searchByName}
 */
@Deprecated
public class ProductSearchSyncService {
    // 已廢棄：ES 同步邏輯移除，商品搜尋改由 MongoDB $text index 處理
}

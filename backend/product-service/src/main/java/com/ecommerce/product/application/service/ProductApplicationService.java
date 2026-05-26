package com.ecommerce.product.application.service;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.repository.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品應用服務（Application Service）
 *
 * <h3>Redis 快取策略</h3>
 * <ul>
 *   <li>{@code products::all}        — 所有上架商品（TTL=10min）</li>
 *   <li>{@code products::id::{id}}   — 單一商品詳情（TTL=10min）</li>
 *   <li>{@code products::cat::{cat}} — 分類商品列表（TTL=10min）</li>
 * </ul>
 * <p>商品新增/刪除時透過 {@code @CacheEvict} 清除相關快取，確保資料一致性。</p>
 */
@Service
@RequiredArgsConstructor
public class ProductApplicationService {

    private final IProductRepository productRepository;

    /** 查詢所有上架商品（Redis 快取，TTL=10min） */
    @Cacheable(value = "products", key = "'all'")
    public List<Product> getAllActiveProducts() {
        return productRepository.findAllActive();
    }

    /** 根據 ID 查詢商品（Redis 快取，TTL=10min） */
    @Cacheable(value = "products", key = "'id::' + #id")
    public Product getProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在：" + id));
    }

    /** 依分類查詢上架商品（Redis 快取，TTL=10min） */
    @Cacheable(value = "products", key = "'cat::' + #category")
    public List<Product> getByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    /**
     * MongoDB $text 全文搜尋（不快取，即時依 textScore 排序）
     *
     * <p>搜尋關鍵字組合無限，快取命中率低，不予快取。</p>
     */
    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByName(keyword);
    }

    /** 新增商品，清除 all 與對應分類快取 */
    @Caching(evict = {
        @CacheEvict(value = "products", key = "'all'"),
        @CacheEvict(value = "products", key = "'cat::' + #product.category")
    })
    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    /** 刪除商品，清除 all / id 快取 */
    @Caching(evict = {
        @CacheEvict(value = "products", key = "'all'"),
        @CacheEvict(value = "products", key = "'id::' + #id")
    })
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}

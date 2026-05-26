package com.ecommerce.product.infrastructure.persistence.jpa;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.repository.IProductRepository;
import com.ecommerce.product.infrastructure.persistence.mapper.ProductEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 商品 Repository 實作（MongoDB）
 *
 * <p>Domain ↔ Entity 轉換由 {@link ProductEntityMapper}（MapStruct 編譯期生成）處理。</p>
 *
 * <h3>搜尋實作說明</h3>
 * <p>{@link #searchByText} 使用 MongoDB {@code $text} 索引，
 * 依 {@code textScore} 相關度降冪排序，取代原有 regex LIKE 查詢。
 * 索引由 {@link com.ecommerce.product.infrastructure.config.MongoConfig} 啟動時建立。</p>
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements IProductRepository {

    private final ProductMongoRepository mongoRepository;
    private final ProductEntityMapper    entityMapper;

    @Override
    public Product save(Product p) {
        return entityMapper.toDomain(
                mongoRepository.save(entityMapper.toEntity(p)));
    }

    @Override
    public Optional<Product> findById(String id) {
        return mongoRepository.findById(id).map(entityMapper::toDomain);
    }

    @Override
    public List<Product> findAllActive() {
        return mongoRepository.findByActiveTrue().stream()
                .map(entityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByCategory(String category) {
        return mongoRepository.findByCategoryAndActiveTrue(category).stream()
                .map(entityMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * $text 全文搜尋，依相關度分數降冪排序
     *
     * @param keyword 搜尋關鍵字（支援空格分詞，如 "iPhone Pro"）
     */
    @Override
    public List<Product> searchByName(String keyword) {
        // Sort.by("score") 搭配 @TextScore 欄位，由 MongoDB 依 textScore 排序
        Sort byScore = Sort.by(Sort.Direction.DESC, "score");
        return mongoRepository.findByTextAndActiveTrue(keyword, byScore).stream()
                .map(entityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }
}

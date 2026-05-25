package com.ecommerce.product.infrastructure.persistence.jpa;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.repository.IProductRepository;
import com.ecommerce.product.infrastructure.persistence.mapper.ProductEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 商品 Repository 實作（MongoDB）
 *
 * <p>Domain ↔ Entity 轉換改由 {@link ProductEntityMapper}（MapStruct 編譯期生成）處理，
 * 取代原有的 {@code ProductMongoEntity.fromDomain()} / {@code toDomain()} 手動轉換。</p>
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements IProductRepository {

    private final ProductMongoRepository mongoRepository;
    private final ProductEntityMapper    entityMapper;   // MapStruct 生成

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
        return mongoRepository.findByCategory(category).stream()
                .map(entityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> searchByName(String keyword) {
        return mongoRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(entityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }
}

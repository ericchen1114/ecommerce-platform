package com.ecommerce.product.infrastructure.search;

import com.ecommerce.product.domain.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

/**
 * 商品資料同步到 Elasticsearch
 * 在 ProductApplicationService 儲存/更新/刪除後呼叫
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class ProductSearchSyncService {

    private final ProductSearchRepository searchRepository;

    public void sync(Product product) {
        ProductDocument doc = ProductDocument.builder()
                .id(String.valueOf(product.getId()))
                .name(product.getName())
                .description(product.getDescription())
                .category(product.getCategory())
                .price(product.getPrice())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .specifications(product.getSpecifications())
                .tags(product.getTags())
                .active(product.getActive())
                .updatedAt(product.getUpdatedAt())
                .build();

        searchRepository.save(doc);
        log.info("[ES-SYNC] 商品已同步 id={} name={}", product.getId(), product.getName());
    }

    public void delete(Long productId) {
        searchRepository.deleteById(String.valueOf(productId));
        log.info("[ES-SYNC] 商品已從 ES 刪除 id={}", productId);
    }
}

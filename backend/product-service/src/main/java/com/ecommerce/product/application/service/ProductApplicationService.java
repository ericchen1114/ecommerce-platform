package com.ecommerce.product.application.service;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.domain.repository.IProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductApplicationService {

    private final IProductRepository productRepository;

    public List<Product> getAllActiveProducts() {
        return productRepository.findAllActive();
    }

    public Product getProductById(String id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("商品不存在：" + id));
    }

    public List<Product> getByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String keyword) {
        return productRepository.searchByName(keyword);
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }
}

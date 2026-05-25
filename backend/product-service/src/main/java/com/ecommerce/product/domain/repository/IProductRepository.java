package com.ecommerce.product.domain.repository;

import com.ecommerce.product.domain.model.Product;
import java.util.List;
import java.util.Optional;

public interface IProductRepository {
    Product save(Product product);
    Optional<Product> findById(String id);
    List<Product> findAllActive();
    List<Product> findByCategory(String category);
    List<Product> searchByName(String keyword);
    void deleteById(String id);
}

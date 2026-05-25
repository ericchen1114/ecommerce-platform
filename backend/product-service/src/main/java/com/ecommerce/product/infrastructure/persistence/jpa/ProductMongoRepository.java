package com.ecommerce.product.infrastructure.persistence.jpa;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductMongoRepository extends MongoRepository<ProductMongoEntity, String> {
    List<ProductMongoEntity> findByActiveTrue();
    List<ProductMongoEntity> findByCategory(String category);
    List<ProductMongoEntity> findByNameContainingIgnoreCase(String name);
}

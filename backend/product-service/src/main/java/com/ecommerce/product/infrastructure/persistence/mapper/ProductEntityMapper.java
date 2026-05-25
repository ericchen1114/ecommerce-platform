package com.ecommerce.product.infrastructure.persistence.mapper;

import com.ecommerce.product.domain.model.Product;
import com.ecommerce.product.infrastructure.persistence.jpa.ProductMongoEntity;
import org.mapstruct.Mapper;

/**
 * 商品 MongoDB Entity ↔ Domain Model 轉換器（MapStruct）
 *
 * <p>取代 {@link ProductMongoEntity#fromDomain(Product)} 和 {@link ProductMongoEntity#toDomain()} 手動轉換，
 * 由 MapStruct 在編譯期自動生成實作，零反射、零執行期開銷。</p>
 *
 * <h3>欄位對應（全部同名同型別，MapStruct 自動對應）</h3>
 * <pre>
 * ProductMongoEntity  ←→  Product（domain）
 * id                  ←→  id
 * name                ←→  name
 * description         ←→  description
 * price               ←→  price
 * stock               ←→  stock
 * category            ←→  category
 * imageUrl            ←→  imageUrl
 * images              ←→  images（List&lt;String&gt;，自動對應）
 * active              ←→  active
 * createdAt           ←→  createdAt
 * </pre>
 *
 * <h3>Spring 整合</h3>
 * <p>透過 pom.xml 設定 {@code -Amapstruct.defaultComponentModel=spring}，
 * 生成的實作類別自動標注 {@code @Component}，可直接 {@code @Autowired} 注入。</p>
 */
@Mapper
public interface ProductEntityMapper {

    /**
     * Domain Model → MongoDB Entity（用於儲存到 MongoDB）
     *
     * @param product 商品 Domain Model
     * @return        對應的 MongoDB Entity，可直接傳入 {@code MongoRepository.save()}
     */
    ProductMongoEntity toEntity(Product product);

    /**
     * MongoDB Entity → Domain Model（從 MongoDB 讀取後還原）
     *
     * @param entity 從 MongoDB 讀取的 Document Entity
     * @return       商品 Domain Model
     */
    Product toDomain(ProductMongoEntity entity);
}

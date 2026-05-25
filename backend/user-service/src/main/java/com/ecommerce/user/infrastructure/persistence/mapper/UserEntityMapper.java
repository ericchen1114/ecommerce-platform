package com.ecommerce.user.infrastructure.persistence.mapper;

import com.ecommerce.user.domain.model.User;
import com.ecommerce.user.infrastructure.persistence.jpa.UserJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 會員 JPA Entity ↔ Domain Model 轉換器（MapStruct）
 *
 * <p>取代 {@link UserJpaEntity#fromDomain(User)} 和 {@link UserJpaEntity#toDomain()} 手動轉換，
 * 由 MapStruct 在編譯期自動生成實作，零反射、零執行期開銷。</p>
 *
 * <h3>欄位對應（同名自動對應，無需額外標注）</h3>
 * <pre>
 * UserJpaEntity  ←→  User（domain）
 * id             ←→  id
 * memberNo       ←→  memberNo
 * email          ←→  email
 * phone          ←→  phone
 * password       ←→  password
 * fullName       ←→  fullName
 * birthday       ←→  birthday
 * address        ←→  address
 * role           ←→  role（User.UserRole enum，同名同型別，自動對應）
 * createdAt      ←→  createdAt
 * updatedAt      ←→  updatedAt
 * </pre>
 *
 * <h3>Spring 整合</h3>
 * <p>透過 pom.xml 設定 {@code -Amapstruct.defaultComponentModel=spring}，
 * 生成的實作類別自動標注 {@code @Component}，可直接 {@code @Autowired} 注入。</p>
 */
@Mapper
public interface UserEntityMapper {

    /**
     * Domain Model → JPA Entity（用於儲存到 DB）
     *
     * @param user 會員 Domain Model
     * @return     對應的 JPA Entity，可直接傳入 {@code JpaRepository.save()}
     */
    UserJpaEntity toEntity(User user);

    /**
     * JPA Entity → Domain Model（從 DB 讀取後還原）
     *
     * @param entity 從 DB 讀取的 JPA Entity
     * @return       會員 Domain Model
     */
    User toDomain(UserJpaEntity entity);
}

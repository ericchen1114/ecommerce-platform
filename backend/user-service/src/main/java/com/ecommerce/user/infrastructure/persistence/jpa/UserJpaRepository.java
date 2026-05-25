package com.ecommerce.user.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByEmail(String email);
    Optional<UserJpaEntity> findByPhone(String phone);
    Optional<UserJpaEntity> findByMemberNo(String memberNo);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByMemberNo(String memberNo);
}

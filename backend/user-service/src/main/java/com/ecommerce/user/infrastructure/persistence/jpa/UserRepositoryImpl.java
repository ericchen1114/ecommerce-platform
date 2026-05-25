package com.ecommerce.user.infrastructure.persistence.jpa;

import com.ecommerce.user.domain.model.User;
import com.ecommerce.user.domain.repository.IUserRepository;
import com.ecommerce.user.infrastructure.persistence.mapper.UserEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * {@link IUserRepository} 的 JPA 實作
 *
 * <p>透過 {@link UserEntityMapper}（MapStruct 自動生成）完成
 * Domain {@link User} ↔ JPA {@link UserJpaEntity} 的雙向轉換，
 * 取代原有手動 {@code fromDomain()} / {@code toDomain()} 呼叫。</p>
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements IUserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserEntityMapper  userEntityMapper;

    /**
     * {@inheritDoc}
     *
     * <p>流程：Domain → {@code mapper.toEntity()} → JPA save →
     * {@code mapper.toDomain()}（回填 DB 自動生成的 id）</p>
     */
    @Override
    public User save(User user) {
        UserJpaEntity entity = userEntityMapper.toEntity(user);
        UserJpaEntity saved  = jpaRepository.save(entity);
        return userEntityMapper.toDomain(saved);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email)
                .map(userEntityMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> findByPhone(String phone) {
        return jpaRepository.findByPhone(phone)
                .map(userEntityMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public Optional<User> findByMemberNo(String memberNo) {
        return jpaRepository.findByMemberNo(memberNo)
                .map(userEntityMapper::toDomain);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByPhone(String phone) {
        return jpaRepository.existsByPhone(phone);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByMemberNo(String memberNo) {
        return jpaRepository.existsByMemberNo(memberNo);
    }
}

package com.ecommerce.order.infrastructure.persistence.jpa;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.repository.IOrderRepository;
import com.ecommerce.order.infrastructure.persistence.jdbc.OrderJdbcRepository;
import com.ecommerce.order.infrastructure.persistence.mapper.OrderEntityMapper;
import com.ecommerce.order.infrastructure.persistence.mybatis.OrderMapper;
import com.ecommerce.order.interfaces.dto.OrderSearchQuery;
import com.ecommerce.order.interfaces.dto.OrderSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 訂單 Repository 實作
 *
 * <p>整合三種持久化技術：
 * <ul>
 *   <li>JPA  — 簡單 CRUD（save / findById / findByUserId）</li>
 *   <li>JDBC — 批次操作（batchUpdateStatus）</li>
 *   <li>MyBatis — 複雜動態查詢（searchOrders）</li>
 * </ul>
 * Domain ↔ Entity 轉換改由 {@link OrderEntityMapper}（MapStruct 編譯期生成）處理，
 * 取代原有的 {@code OrderJpaEntity.fromDomain()} / {@code toDomain()} 手動轉換。</p>
 */
@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements IOrderRepository {

    private final OrderJpaRepository  jpaRepository;    // 簡單 CRUD
    private final OrderJdbcRepository jdbcRepository;   // 批次操作
    private final OrderMapper         orderMapper;      // 複雜查詢（MyBatis）
    private final OrderEntityMapper   entityMapper;     // Domain ↔ Entity 轉換（MapStruct）

    @Override
    public Order save(Order order) {
        return entityMapper.toDomain(
                jpaRepository.save(entityMapper.toEntity(order)));
    }

    @Override
    public Optional<Order> findById(Long id) {
        return jpaRepository.findById(id).map(entityMapper::toDomain);
    }

    @Override
    public List<Order> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(entityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public int batchUpdateStatus(List<Long> ids, String status) {
        return jdbcRepository.batchUpdateStatus(ids, status);
    }

    @Override
    public List<OrderSummary> searchOrders(OrderSearchQuery query) {
        // MyBatis ResultMap 直接映射至 OrderSummary，不經 MapStruct
        return orderMapper.searchOrders(query);
    }
}

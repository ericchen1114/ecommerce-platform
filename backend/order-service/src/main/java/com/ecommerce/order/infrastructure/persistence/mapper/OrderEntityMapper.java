package com.ecommerce.order.infrastructure.persistence.mapper;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderItem;
import com.ecommerce.order.infrastructure.persistence.jpa.OrderItemJpaEntity;
import com.ecommerce.order.infrastructure.persistence.jpa.OrderJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 訂單 JPA Entity ↔ Domain Model 轉換器（MapStruct）
 *
 * <p>取代 {@link OrderJpaEntity#fromDomain(Order)} 和 {@link OrderJpaEntity#toDomain()} 手動轉換，
 * 由 MapStruct 在編譯期自動生成實作。</p>
 *
 * <h3>欄位對應</h3>
 * <pre>
 * OrderJpaEntity  ←→  Order（domain）
 * id              ←→  id
 * orderNo         ←→  orderNo
 * userId          ←→  userId
 * orderType       ←→  orderType（enum，同型別自動對應）
 * totalAmount     ←→  totalAmount
 * status          ←→  status（Order.OrderStatus，同型別自動對應）
 * shippingAddress ←→  shippingAddress
 * region          ←→  region
 * memberLevel     ←→  memberLevel
 * member          ←→  member
 * promoCode       ←→  promoCode
 * shippingState   ←→  shippingState
 * note            ←→  note
 * items           ←→  items（集合自動遞迴轉換）
 * createdAt       ←→  createdAt
 *
 * OrderItemJpaEntity  ←→  OrderItem（domain）
 * id                  ←→  id
 * productId           ←→  productId
 * productName         ←→  productName
 * quantity            ←→  quantity
 * price               ←→  price
 * （order 欄位忽略，避免循環依賴）
 * </pre>
 */
@Mapper
public interface OrderEntityMapper {

    /**
     * Domain Model → JPA Entity（用於儲存到 DB）
     *
     * @param order 訂單 Domain Model
     * @return      對應的 JPA Entity，可直接傳入 {@code JpaRepository.save()}
     */
    @Mapping(target = "items", ignore = true)
    OrderJpaEntity toEntity(Order order);

    /**
     * JPA Entity → Domain Model（從 DB 讀取後還原）
     *
     * @param entity 從 DB 讀取的 JPA Entity
     * @return       訂單 Domain Model
     */
    Order toDomain(OrderJpaEntity entity);

    /**
     * OrderItem Domain → OrderItemJpaEntity
     *
     * @param item 訂單明細 Domain Model
     * @return     對應的 JPA Entity
     */
    @Mapping(target = "order", ignore = true)
    OrderItemJpaEntity toItemEntity(OrderItem item);

    /**
     * OrderItemJpaEntity → OrderItem Domain
     *
     * @param entity 訂單明細 JPA Entity
     * @return       訂單明細 Domain Model
     */
    OrderItem toItemDomain(OrderItemJpaEntity entity);
}

package com.ecommerce.order.interfaces.mapper;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.interfaces.dto.OrderSummary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 訂單 DTO ↔ Domain Model 轉換器（MapStruct）
 *
 * <p>負責 interfaces 層（DTO）與 domain 層（Model）之間的轉換。
 * 複雜查詢（MyBatis searchOrders）直接映射至 {@link OrderSummary}，不經此 Mapper。</p>
 *
 * <h3>轉換對照</h3>
 * <pre>
 * Order（domain）   →  OrderSummary
 *   id             →    orderId      （欄位名稱不同，需 @Mapping）
 *   userId         →    userId
 *   status.name()  →    status       （enum → String）
 *   totalAmount    →    totalAmount
 *   createdAt      →    createdAt
 *   （username 由 MyBatis JOIN 查詢填入，此處忽略）
 * </pre>
 */
@Mapper
public interface OrderDtoMapper {

    /**
     * Order（domain）→ OrderSummary
     *
     * <p>{@code username} 欄位需要 JOIN 查詢，此處設為 ignore；
     * MyBatis searchOrders 的 ResultMap 直接填入完整欄位。</p>
     *
     * @param order 訂單 Domain Model
     * @return      API 回應用的訂單摘要 DTO
     */
    @Mapping(target = "orderId",  source = "id")
    @Mapping(target = "status",   expression = "java(order.getStatus().name())")
    @Mapping(target = "username", ignore = true)
    OrderSummary toOrderSummary(Order order);
}

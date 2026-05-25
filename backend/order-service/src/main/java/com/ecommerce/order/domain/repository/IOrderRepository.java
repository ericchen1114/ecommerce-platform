package com.ecommerce.order.domain.repository;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.interfaces.dto.OrderSearchQuery;
import com.ecommerce.order.interfaces.dto.OrderSummary;
import java.util.List;
import java.util.Optional;

public interface IOrderRepository {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findByUserId(Long userId);                          // JPA
    int batchUpdateStatus(List<Long> ids, String status);          // JDBC
    List<OrderSummary> searchOrders(OrderSearchQuery query);       // MyBatis
}

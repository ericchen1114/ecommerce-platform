package com.ecommerce.order.infrastructure.persistence.mybatis;

import com.ecommerce.order.interfaces.dto.OrderSearchQuery;
import com.ecommerce.order.interfaces.dto.OrderSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderMapper {
    List<OrderSummary> searchOrders(@Param("query") OrderSearchQuery query);
}

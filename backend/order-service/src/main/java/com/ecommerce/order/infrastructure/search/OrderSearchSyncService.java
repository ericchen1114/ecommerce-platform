package com.ecommerce.order.infrastructure.search;

import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.model.OrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 訂單事件同步到 Elasticsearch
 * 監聽 RabbitMQ ORDER_CONFIRMED 事件，建立複合文件
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class OrderSearchSyncService {

    private final OrderSearchRepository searchRepository;

    /**
     * 訂單確認後同步到 ES
     * 在 ORDER_CONFIRMED Saga 事件處理完成後呼叫
     */
    public void syncOrder(Order order) {
        List<OrderDocument.OrderItemSnapshot> itemSnapshots = order.getItems()
                .stream()
                .map(item -> OrderDocument.OrderItemSnapshot.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())     // 快照欄位
                        .unitPrice(item.getUnitPrice())         // 快照欄位
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        OrderDocument doc = OrderDocument.builder()
                .orderId(String.valueOf(order.getId()))
                .userId(order.getUserId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .items(itemSnapshots)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();

        searchRepository.save(doc);
        log.info("[ES-SYNC] 訂單已同步 orderId={} userId={}",
                order.getId(), order.getUserId());
    }

    /**
     * 訂單狀態更新（CANCELLED 等）同步到 ES
     */
    public void updateOrderStatus(String orderId, String status) {
        searchRepository.findById(orderId).ifPresent(doc -> {
            doc.setStatus(status);
            searchRepository.save(doc);
            log.info("[ES-SYNC] 訂單狀態已更新 orderId={} status={}", orderId, status);
        });
    }
}

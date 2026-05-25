package com.ecommerce.order.infrastructure.search;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Elasticsearch 訂單複合文件
 * Index：ecommerce-orders
 *
 * 包含訂單基本資訊 + 商品快照（來自 order_items）
 * 用途：
 *   - 後台訂單搜尋（含商品名稱關鍵字）
 *   - 按品類統計訂單量
 *   - 用戶訂單歷史全文搜尋
 */
@Document(indexName = "ecommerce-orders")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDocument {

    @Id
    private String orderId;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Double)
    private BigDecimal totalAmount;

    @Field(type = FieldType.Text)
    private String shippingAddress;

    // 訂單項目（巢狀文件，含商品快照）
    @Field(type = FieldType.Nested)
    private List<OrderItemSnapshot> items;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime updatedAt;

    // ── 巢狀商品快照 ─────────────────────────────────────────
    @Getter @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemSnapshot {

        @Field(type = FieldType.Long)
        private Long productId;

        @Field(type = FieldType.Text, analyzer = "standard")
        private String productName;     // 快照

        @Field(type = FieldType.Keyword)
        private String category;        // 快照（供品類統計）

        @Field(type = FieldType.Double)
        private BigDecimal unitPrice;   // 快照

        @Field(type = FieldType.Integer)
        private Integer quantity;
    }
}

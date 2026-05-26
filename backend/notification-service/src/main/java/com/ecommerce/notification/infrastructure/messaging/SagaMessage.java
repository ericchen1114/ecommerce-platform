package com.ecommerce.notification.infrastructure.messaging;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Saga 通知訊息 DTO（notification-service 用）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaMessage {
    private String messageId;
    private String eventType;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String notificationContent;
    private LocalDateTime createdAt;
}

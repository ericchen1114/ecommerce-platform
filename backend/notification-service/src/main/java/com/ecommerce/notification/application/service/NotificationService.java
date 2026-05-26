package com.ecommerce.notification.application.service;

import com.ecommerce.notification.infrastructure.messaging.SagaMessage;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 通知服務（notification-service）
 *
 * <p>監聽 Saga {@code notification.queue}，接收訂單/付款通知訊息，
 * 依 {@code eventType} 路由至對應發送邏輯。</p>
 *
 * <h3>支援的通知類型</h3>
 * <ul>
 *   <li>{@code NOTIFICATION} — 通用通知（訂單確認、付款失敗等）</li>
 * </ul>
 *
 * <h3>擴充說明</h3>
 * <p>目前以 Log 模擬發送。實際接入時替換 {@link #sendEmail} / {@link #sendSms}
 * 為第三方 SDK（SendGrid / AWS SES / Twilio）即可，無需修改 Consumer 邏輯。</p>
 */
@Slf4j
@Service
public class NotificationService {

    private static final String Q_NOTIFICATION = "notification.queue";

    /**
     * 消費通知訊息（MANUAL ACK）
     *
     * <p>使用手動 ACK 確保通知確實發送後才確認訊息，
     * 避免服務重啟時通知遺失。</p>
     */
    @RabbitListener(queues = Q_NOTIFICATION,
                    ackMode = "MANUAL",
                    containerFactory = "rabbitListenerContainerFactory")
    public void handleNotification(SagaMessage message,
                                   Channel channel,
                                   @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("[NotificationService] 收到通知，messageId={}, eventType={}, orderId={}",
                message.getMessageId(), message.getEventType(), message.getOrderId());

        try {
            String content = message.getNotificationContent();
            if (content == null || content.isBlank()) {
                content = buildDefaultContent(message);
            }

            // 發送 Email（依 userId 查詢 Email，此處以 log 模擬）
            sendEmail(message.getUserId(), content);

            // 發送 SMS（依需求可選）
            sendSms(message.getUserId(), content);

            channel.basicAck(deliveryTag, false);
            log.info("[NotificationService] 通知發送完成，orderId={}", message.getOrderId());

        } catch (Exception e) {
            log.error("[NotificationService] 通知發送失敗，orderId={}：{}", message.getOrderId(), e.getMessage(), e);
            // requeue=false 讓失敗訊息進 DLQ，避免無限重試
            channel.basicNack(deliveryTag, false, false);
        }
    }

    // ─── 發送通道（實際應注入第三方 SDK）────────────────────────────────────────

    /**
     * 發送 Email
     *
     * <p>TODO: 替換為 SendGrid / AWS SES / JavaMailSender</p>
     */
    private void sendEmail(Long userId, String content) {
        // 實際應查詢 user-service 取得 email，此處以 log 模擬
        log.info("[NotificationService] 📧 Email → userId={} | 內容：{}", userId, content);
    }

    /**
     * 發送 SMS
     *
     * <p>TODO: 替換為 Twilio / 三竹簡訊 API</p>
     */
    private void sendSms(Long userId, String content) {
        log.info("[NotificationService] 📱 SMS → userId={} | 內容：{}", userId, content);
    }

    // ─── 預設內容建構 ──────────────────────────────────────────────────────────

    private String buildDefaultContent(SagaMessage message) {
        return switch (message.getEventType() != null ? message.getEventType() : "") {
            case "NOTIFICATION" -> String.format("您的訂單 %s 有更新，請至會員中心查看。", message.getOrderNo());
            default             -> String.format("系統通知（訂單 %s）", message.getOrderNo());
        };
    }
}

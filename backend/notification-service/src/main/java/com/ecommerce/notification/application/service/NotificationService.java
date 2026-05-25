package com.ecommerce.notification.application.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    @RabbitListener(queues = "order.notification.queue")
    public void handleOrderNotification(String message) {
        log.info("📧 收到訂單通知: {}", message);
        // TODO: 串接 Email / SMS
    }

    @RabbitListener(queues = "payment.notification.queue")
    public void handlePaymentNotification(String message) {
        log.info("💳 收到付款通知: {}", message);
    }
}

package com.ecommerce.order.infrastructure.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Saga Choreography RabbitMQ 設定（order-service）
 *
 * <h3>Exchange 架構</h3>
 * <pre>
 * saga.exchange (direct)
 *   routing key: payment.request  → payment.request.queue
 *   routing key: payment.result   → payment.result.queue   (order-service 消費)
 *   routing key: stock.request    → stock.request.queue    (product-service 消費)
 *   routing key: stock.result     → stock.result.queue     (order-service 消費)
 *   routing key: notification     → notification.queue     (notification-service 消費)
 *
 * saga.dlx (Dead Letter Exchange, fanout)
 *   → saga.dlq (Dead Letter Queue，人工審查補償失敗訊息)
 * </pre>
 *
 * <h3>DLQ 設計</h3>
 * <p>每個業務 Queue 設定 {@code x-dead-letter-exchange=saga.dlx}，
 * 當訊息被 NACK 且 {@code requeue=false} 時自動進入 DLQ，
 * 避免無限重試造成的訊息堆積。</p>
 */
@Configuration
public class SagaRabbitMQConfig {

    // ─── Exchange Names ───────────────────────────────────────────────────────
    public static final String SAGA_EXCHANGE   = "saga.exchange";
    public static final String SAGA_DLX        = "saga.dlx";

    // ─── Routing Keys ─────────────────────────────────────────────────────────
    public static final String RK_PAYMENT_REQUEST  = "payment.request";
    public static final String RK_PAYMENT_RESULT   = "payment.result";
    public static final String RK_STOCK_REQUEST    = "stock.request";
    public static final String RK_STOCK_RESULT     = "stock.result";
    public static final String RK_NOTIFICATION     = "notification";

    // ─── Queue Names ──────────────────────────────────────────────────────────
    public static final String Q_PAYMENT_REQUEST   = "payment.request.queue";
    public static final String Q_PAYMENT_RESULT    = "payment.result.queue";
    public static final String Q_STOCK_REQUEST     = "stock.request.queue";
    public static final String Q_STOCK_RESULT      = "stock.result.queue";
    public static final String Q_NOTIFICATION      = "notification.queue";
    public static final String Q_SAGA_DLQ          = "saga.dlq";

    // ─── Exchanges ───────────────────────────────────────────────────────────
    @Bean
    public DirectExchange sagaExchange() {
        return ExchangeBuilder.directExchange(SAGA_EXCHANGE).durable(true).build();
    }

    /** Dead Letter Exchange（fanout，所有 DLQ 訊息集中到一個 DLQ 方便審查） */
    @Bean
    public FanoutExchange sagaDlx() {
        return ExchangeBuilder.fanoutExchange(SAGA_DLX).durable(true).build();
    }

    // ─── Queues（含 DLQ 設定）────────────────────────────────────────────────
    private QueueBuilder durableWithDlx() {
        return QueueBuilder.durable().withArgument("x-dead-letter-exchange", SAGA_DLX);
    }

    @Bean public Queue paymentRequestQueue()  { return durableWithDlx().withName(Q_PAYMENT_REQUEST).build(); }
    @Bean public Queue paymentResultQueue()   { return durableWithDlx().withName(Q_PAYMENT_RESULT).build(); }
    @Bean public Queue stockRequestQueue()    { return durableWithDlx().withName(Q_STOCK_REQUEST).build(); }
    @Bean public Queue stockResultQueue()     { return durableWithDlx().withName(Q_STOCK_RESULT).build(); }
    @Bean public Queue notificationQueue()    { return durableWithDlx().withName(Q_NOTIFICATION).build(); }

    /** DLQ 本身不需要 DLX（防止無限循環） */
    @Bean
    public Queue sagaDlq() {
        return QueueBuilder.durable(Q_SAGA_DLQ).build();
    }

    // ─── Bindings ────────────────────────────────────────────────────────────
    @Bean public Binding bindPaymentRequest(Queue paymentRequestQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(paymentRequestQueue).to(sagaExchange).with(RK_PAYMENT_REQUEST);
    }
    @Bean public Binding bindPaymentResult(Queue paymentResultQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(paymentResultQueue).to(sagaExchange).with(RK_PAYMENT_RESULT);
    }
    @Bean public Binding bindStockRequest(Queue stockRequestQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(stockRequestQueue).to(sagaExchange).with(RK_STOCK_REQUEST);
    }
    @Bean public Binding bindStockResult(Queue stockResultQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(stockResultQueue).to(sagaExchange).with(RK_STOCK_RESULT);
    }
    @Bean public Binding bindNotification(Queue notificationQueue, DirectExchange sagaExchange) {
        return BindingBuilder.bind(notificationQueue).to(sagaExchange).with(RK_NOTIFICATION);
    }
    @Bean public Binding bindSagaDlq(Queue sagaDlq, FanoutExchange sagaDlx) {
        return BindingBuilder.bind(sagaDlq).to(sagaDlx);
    }

    // ─── JSON Message Converter ───────────────────────────────────────────────
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}

# 電商平台專案 CLAUDE.md
> 每次開新 session 自動載入，節省 token

## 技術棧
- 後端：Spring Boot 3.2 + Spring Cloud 2023（Java 21）
- 前端：Vue 3 + Vite（前台 :5173 / 後台 :5174）
- 架構：DDD 四層（interfaces / application / domain / infrastructure）

## Spring Cloud 完整元件
| 元件 | 服務 | Port |
|------|------|------|
| Eureka Server | 服務發現 | 8761 |
| Config Server | 集中配置 | 8888 |
| API Gateway | Spring Cloud Gateway + JWT | 8080 |
| user-service | 會員 / JWT | 8081 |
| product-service | 商品 / MongoDB | 8082 |
| order-service | 訂單 / Saga 編排 | 8083 |
| payment-service | 付款 | 8084 |
| notification-service | 通知 / RabbitMQ Consumer | 8085 |

## 基礎設施
- MySQL 8.0 :3306
- MongoDB 7.0 :27017
- Redis 7 :6379
- RabbitMQ 3 :5672 / UI :15672
- Kafka :9092
- Elasticsearch 8 :9200
- Kibana :5601
- Zipkin :9411
- Nginx :80

## 關鍵架構設計
- 持久層：JPA（CRUD）+ JDBC（批次）+ MyBatis（複雜查詢）共存
- 服務間呼叫：OpenFeign + Eureka LoadBalancer（不用硬編碼 URL）
- 熔斷：Resilience4j（CB + Retry + TimeLimiter）
- 分布式事務：Saga Choreography + RabbitMQ + DLQ
- 追蹤：Micrometer Tracing + Zipkin（traceId 全鏈路一致）
- 日誌：Log4j2 + AOP 自動記錄所有 Request/Response

## 開發原則
- 所有 container 互連用 service name，不用 localhost
- JWT 在 Gateway 統一驗證，下游服務讀 X-User-Id header
- 集中設定在 config-repo/，修改後 /actuator/refresh 熱更新
- Domain 層純 Java，不依賴任何框架

## 服務啟動順序
1. mysql / mongodb / redis / rabbitmq / kafka
2. eureka-server
3. config-server
4. api-gateway
5. user/product/order/payment/notification service

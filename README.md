# 微服務電商平台

> Spring Cloud × DDD × 設計模式 × 全鏈路可觀測性

企業級生產標準建構的電商後端系統，涵蓋完整微服務架構、分散式事務、事件驅動設計，並配備前後台雙端 Vue 3 介面。

---

## 架構總覽

```
┌──────────────────────────────────────────────────────────────┐
│              Vue 3 前台（:5173）  ╱  Vue 3 後台（:5174）      │
└───────────────────────────┬──────────────────────────────────┘
                             │ HTTP
┌───────────────────────────▼──────────────────────────────────┐
│                   API Gateway（:8080）                         │
│          JWT 驗證 · 路由轉發 · Rate Limiting                   │
└──────┬──────────┬──────────┬──────────┬────────────────────── ┘
       │          │          │          │
  user-service  product-  order-    payment-   notification-
    :8081       service   service    service      service
                 :8082     :8083      :8084        :8085
┌──────────────────────────────────────────────────────────────┐
│     Eureka :8761  │  Config Server :8888  │  Zipkin :9411     │
├──────────────────────────────────────────────────────────────┤
│     MySQL :3306   │  MongoDB :27017  │  Redis :6379           │
├──────────────────────────────────────────────────────────────┤
│  RabbitMQ :5672/:15672（主力）│  Kafka :9092（擴充預留）      │
└──────────────────────────────────────────────────────────────┘
```

---

## 技術棧

| 分類 | 技術 |
|------|------|
| **後端框架** | Spring Boot 3.2、Spring Cloud 2023（Gateway / Eureka / Config / OpenFeign）|
| **程式語言** | Java 21 |
| **架構模式** | DDD 四層、CQRS、Saga Choreography、Event-Driven |
| **設計模式** | Factory、Strategy、Observer、Repository、Mapper（MapStruct）|
| **關聯式資料庫** | MySQL 8（主從讀寫分離）— 用戶、訂單、付款（ACID 強一致性）|
| **文件型資料庫** | MongoDB 7 — 商品目錄（彈性欄位、巢狀圖片陣列、$text 全文搜尋）|
| **快取** | Redis 7（商品快取、訂單號原子生成）|
| **訊息佇列** | RabbitMQ 3（主力：Saga 事件驅動、DLQ 補償、非同步通知）|
| **串流擴充** | Kafka（預留：高吞吐量日誌串流、未來行為分析擴充用，現階段不承載業務流程）|
| **穩定性** | Resilience4j（CircuitBreaker / Retry / TimeLimiter）|
| **可觀測性** | Micrometer Tracing + Zipkin、Log4j2 AOP + MDC、ELK Stack（日誌索引）|
| **API 文件** | SpringDoc OpenAPI 2.3（Swagger UI，Gateway 聚合 6 服務）|
| **安全性** | BCrypt、JWT Bearer（Gateway 統一驗證）、Spring Security Stateless |
| **物件映射** | MapStruct 1.5.5（編譯期生成，零反射）|
| **前端** | Vue 3 + Vite + Element Plus + Pinia |
| **容器化** | Docker + Docker Compose（13 個容器）|

---

## 微服務清單

| 服務 | Port | 職責 |
|------|------|------|
| `api-gateway` | 8080 | JWT 驗證、路由轉發、全局過濾、Rate Limiting |
| `user-service` | 8081 | 會員認證、JWT 簽發、BCrypt 加密 |
| `product-service` | 8082 | 商品 CRUD、MongoDB $text 全文搜尋、Redis 快取 |
| `order-service` | 8083 | Saga 協調、Factory + Strategy + Observer 三模式 |
| `payment-service` | 8084 | Saga Consumer、付款狀態機、補償事務 |
| `notification-service` | 8085 | RabbitMQ Consumer、Email / SMS 非同步發送 |
| `eureka-server` | 8761 | 服務發現與健康監控 |
| `config-server` | 8888 | 集中設定管理、熱更新 `/actuator/refresh` |

---

## 核心設計亮點

### DDD 四層架構（每個服務統一）

```
interfaces/     → REST Controller、DTO、GlobalExceptionHandler
application/    → Application Service（薄層協調）、CQRS Command/Query
domain/         → Entity、Value Object、Domain Event、Repository Interface（純 Java）
infrastructure/ → JPA / JDBC / MyBatis 共存、RabbitMQ、Redis、MapStruct Mapper
```

### 設計模式落地（order-service）

**Factory — 訂單建立解耦**
`OrderFactory` interface + `StandardOrderFactory` / `GiftCardOrderFactory` / `SubscriptionOrderFactory`。
`OrderFactorySelector` 透過 Spring 注入 `List<OrderFactory>` 自動選取，新增訂單類型零改 Service。

**Strategy — 計價規則組合**
四個策略依序執行：`MemberDiscount` → `Promotion` → `RegionalPricing` → `Tax`。
各策略獨立可測試，促銷規則可熱插拔。

**Observer — 訂單事件廣播**
訂單持久化後 `OrderEventPublisher.publish()` 廣播事件，`PaymentEventListener` / `NotificationEventListener` / `LoyaltyEventListener` 非同步並行，任一 Listener 異常不影響主流程。

### Saga Choreography（分散式事務）

```
下單 → ORDER_CREATED
  → product-service 扣庫存 → STOCK_RESERVED / STOCK_FAILED
    → payment-service 扣款  → PAYMENT_COMPLETED / PAYMENT_FAILED
      ✅ ORDER_CONFIRMED（成功）
      ❌ ORDER_CANCELLED + STOCK_RELEASED（失敗補償）
```

RabbitMQ DLQ 承接無法處理的補償訊息；`SagaState` 表保證冪等性，支援重試不重複執行。

### 訊息佇列選型：RabbitMQ（主力）vs Kafka（擴充預留）

| 面向 | RabbitMQ | Kafka |
|------|----------|-------|
| 定位 | **本專案主力** | 擴充預留，現階段不承載業務 |
| 使用場景 | Saga 事件（下單/扣款/補償）、DLQ 死信處理、Email/SMS 非同步通知 | 預留高吞吐量場景（行為日誌、未來行銷分析）|
| 訊息模型 | Push-based，Broker 主動推送給 Consumer | Pull-based，Consumer 自行拉取 |
| 訊息確認 | ACK / NACK + 自動重入隊，補償事務可靠性高 | Offset 管理，適合回放但確認機制較複雜 |
| 延遲 | 毫秒級，適合即時業務觸發 | 吞吐量優先，延遲較 RabbitMQ 高 |
| 適用理由 | 電商 Saga 需要精確的訊息確認與 DLQ 補償，RabbitMQ 語意最符合 | 訂單量真正達到百萬級後，行為數據串流再引入 Kafka 叢集 |

### 訂單號生成（Redis + DB 雙層備援）

- **正常**：Redis `INCR` 原子操作，格式 `ORD{yyyyMMdd}{6位序號}`
- **故障**：自動降級到 `DB MAX + 1`
- **恢復**：同步腳本將 Redis 計數器更新至 DB 最大值，防序號跳號

### 資料庫選型說明

| 場景 | 使用 | 理由 |
|------|------|------|
| 用戶帳戶、訂單、付款 | MySQL | 跨表 JOIN、ACID 事務、主從讀寫分離 |
| 商品目錄 | MongoDB | 商品規格欄位不固定（服飾尺寸≠3C規格）、巢狀圖片陣列、$text 全文搜尋 |
| 商品搜尋 | MongoDB $text 索引 | 現階段商品量級足夠；避免引入 ES 的額外 JVM heap（4–8 GB）與同步維運成本 |
| 日誌索引 | ELK Stack | Log4j2 AOP 產生的日誌集中至 Elasticsearch，Kibana 可視化查詢 |

---

## 快速啟動

### 環境需求

Java 21+、Maven 3.9+、Node 18+、Docker Desktop

### 1. 複製設定檔

```bash
cp .env.example .env
# 編輯 .env，填入你的密碼與金鑰
```

### 2. 啟動基礎設施

```bash
docker compose up -d
```

### 3. 依序啟動後端服務

```bash
# ① 服務發現
cd backend/eureka-server && ./mvnw spring-boot:run &

# ② 集中配置（等 Eureka 就緒後）
cd backend/config-server && ./mvnw spring-boot:run &

# ③ 業務服務（可同時啟動）
cd backend/user-service         && ./mvnw spring-boot:run &
cd backend/product-service      && ./mvnw spring-boot:run &
cd backend/order-service        && ./mvnw spring-boot:run &
cd backend/payment-service      && ./mvnw spring-boot:run &
cd backend/notification-service && ./mvnw spring-boot:run &

# ④ API Gateway（最後）
cd backend/api-gateway && ./mvnw spring-boot:run &
```

### 4. 啟動前端

```bash
cd frontend/shop-frontend  && npm install && npm run dev   # :5173
cd frontend/admin-frontend && npm install && npm run dev   # :5174
```

---

## 常用連結

| 服務 | URL |
|------|-----|
| 前台購物網站 | http://localhost:5173 |
| 後台管理系統 | http://localhost:5174 |
| Swagger UI（聚合 6 服務）| http://localhost:8080/swagger-ui/index.html |
| Eureka Dashboard | http://localhost:8761 （admin / 見 .env）|
| RabbitMQ Management | http://localhost:15672 （admin / 見 .env）|
| Kibana 日誌（ELK）| http://localhost:5601 |
| Zipkin 鏈路追蹤 | http://localhost:9411 |

---

## API 快速測試

```bash
# 1. 確認是否為會員
curl -X POST http://localhost:8080/api/users/check \
  -H "Content-Type: application/json" \
  -d '{"identifier": "0912345678"}'

# 2. 註冊
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"王小明","phone":"0912345678","email":"wang@example.com","birthday":"1990-05-15","password":"Password123!"}'

# 3. 登入取得 Token
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"0912345678","password":"Password123!"}'

# 4. 查詢商品（帶 Token）
curl http://localhost:8080/api/products \
  -H "Authorization: Bearer {token}"
```

---

## 專案結構

```
ecommerce-platform/
├── backend/
│   ├── api-gateway/
│   ├── eureka-server/
│   ├── config-server/
│   ├── user-service/
│   ├── product-service/
│   ├── order-service/
│   ├── payment-service/
│   └── notification-service/
├── frontend/
│   ├── shop-frontend/       # Vue 3 前台
│   └── admin-frontend/      # Vue 3 後台
├── config-repo/             # Spring Cloud Config 集中設定
├── infrastructure/          # Nginx、ELK（Logstash / Filebeat）設定
├── docker-compose.yml
├── .env.example             # 環境變數範本
└── .gitignore
```

---

## 環境變數

複製 `.env.example` 為 `.env` 並填入實際值：

| 變數 | 說明 |
|------|------|
| `MYSQL_ROOT_PASSWORD` | MySQL root 密碼 |
| `RABBITMQ_DEFAULT_PASS` | RabbitMQ 密碼 |
| `JWT_SECRET` | JWT 簽名金鑰（建議 256-bit 以上）|
| `CONFIG_REPO_PATH` | Config Server 設定檔目錄路徑 |
| `ZIPKIN_ENDPOINT` | Zipkin 追蹤端點 |

> ⚠️ `.env` 已加入 `.gitignore`，請勿將實際密碼 commit 至版本控制。

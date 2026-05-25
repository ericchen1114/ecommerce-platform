package com.ecommerce.order.application.service;

import com.ecommerce.order.application.event.OrderEventPublisher;
import com.ecommerce.order.application.factory.OrderFactorySelector;
import com.ecommerce.order.application.pricing.PricingContext;
import com.ecommerce.order.domain.event.OrderConfirmedEvent;
import com.ecommerce.order.domain.event.OrderCreatedEvent;
import com.ecommerce.order.domain.factory.OrderFactory;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.domain.repository.IOrderRepository;
import com.ecommerce.order.infrastructure.config.OrderNoGenerator;
import com.ecommerce.order.interfaces.dto.CreateOrderRequest;
import com.ecommerce.order.interfaces.dto.OrderSearchQuery;
import com.ecommerce.order.interfaces.dto.OrderSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 訂單應用服務（Application Service）
 *
 * <p>整合三個設計模式，協調領域物件完成訂單業務流程：</p>
 *
 * <h3>設計模式整合</h3>
 * <ol>
 *   <li><b>工廠模式</b>（{@link OrderFactorySelector}）：
 *       依訂單類型選取對應工廠建立訂單，新增類型無需修改此 Service</li>
 *   <li><b>策略模式</b>（{@link PricingContext}）：
 *       依序套用會員折扣、促銷碼、地區運費、稅率計算最終金額</li>
 *   <li><b>觀察者模式</b>（{@link OrderEventPublisher}）：
 *       訂單持久化後發布事件，由各監聽器處理付款、通知、積分等後續邏輯</li>
 * </ol>
 *
 * <h3>createOrder 流程</h3>
 * <pre>
 * 1. 工廠模式 → 建立訂單領域物件（含業務規則驗證）
 * 2. 策略模式 → 計算最終金額
 * 3. 生成訂單號（OrderNoGenerator）
 * 4. 持久化訂單（Repository）
 * 5. 觀察者模式 → 發布 OrderCreatedEvent（觸發付款、通知、積分）
 * </pre>
 *
 * <p>此 Service 程式碼精簡至約 50 行核心邏輯，符合單一職責原則（SRP）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private final IOrderRepository      orderRepository;
    private final OrderFactorySelector  factorySelector;
    private final PricingContext        pricingContext;
    private final OrderEventPublisher   eventPublisher;
    private final OrderNoGenerator      orderNoGenerator;

    /**
     * 建立訂單（整合工廠 + 策略 + 觀察者模式）
     *
     * <p>完整流程：選取工廠 → 建立訂單物件 → 計算最終金額 →
     * 生成訂單號 → 持久化 → 發布建立事件。</p>
     *
     * @param req 建立訂單請求 DTO（含訂單類型、商品資訊、計價欄位）
     * @return    已持久化的訂單（含資料庫 ID 與訂單號）
     * @throws com.ecommerce.order.domain.exception.BusinessException 業務規則驗證失敗時
     */
    @Transactional
    public Order createOrder(CreateOrderRequest req) {
        log.info("[OrderApplicationService] 開始建立訂單，userId={}, orderType={}",
                req.getUserId(), req.getOrderType());

        // ① 工廠模式：依訂單類型選取工廠，建立訂單領域物件
        OrderFactory factory = factorySelector.getFactory(req.getOrderType());
        Order order = factory.create(req);

        // ② 策略模式：依序套用所有計價策略計算最終金額
        BigDecimal finalPrice = pricingContext.calculateFinalPrice(order.getTotalAmount(), order);
        order.setTotalAmount(finalPrice);

        // ③ 生成唯一訂單號（Redis INCR + DB 備援）
        String orderNo = orderNoGenerator.generate();
        // 透過 Builder 重建加入 orderNo（Lombok @Builder 不可直接 setOrderNo）
        order = Order.builder()
                .id(order.getId())
                .orderNo(orderNo)
                .userId(order.getUserId())
                .orderType(order.getOrderType())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .shippingAddress(order.getShippingAddress())
                .region(order.getRegion())
                .memberLevel(order.getMemberLevel())
                .member(order.isMember())
                .promoCode(order.getPromoCode())
                .shippingState(order.getShippingState())
                .note(order.getNote())
                .items(order.getItems())
                .createdAt(order.getCreatedAt())
                .build();

        // ④ 持久化訂單
        Order saved = orderRepository.save(order);
        log.info("[OrderApplicationService] 訂單已儲存，orderId={}, orderNo={}, amount={}",
                saved.getId(), saved.getOrderNo(), saved.getTotalAmount());

        // ⑤ 觀察者模式：發布事件，由各監聽器非同步處理後續邏輯
        // OrderApplicationService 不需要知道有哪些監聽器在監聽
        eventPublisher.publish(new OrderCreatedEvent(saved, req));

        return saved;
    }

    /**
     * 根據 ID 查詢訂單
     *
     * @param id 訂單資料庫主鍵
     * @return   對應的訂單領域物件
     * @throws IllegalArgumentException 訂單不存在時拋出
     */
    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在：" + id));
    }

    /**
     * 查詢指定會員的所有訂單
     *
     * @param userId 會員 ID
     * @return       該會員的訂單列表（若無則為空列表）
     */
    @Transactional(readOnly = true)
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    /**
     * JDBC 批次確認多筆訂單
     *
     * <p>透過 JDBC Template 執行批次 UPDATE，效能優於逐筆更新。
     * 適用於後台管理批次審核訂單的場景。</p>
     *
     * @param orderIds 要確認的訂單 ID 列表
     * @return         實際更新的筆數
     */
    @Transactional
    public int batchConfirmOrders(List<Long> orderIds) {
        int updated = orderRepository.batchUpdateStatus(orderIds, "CONFIRMED");
        log.info("[OrderApplicationService] 批次確認訂單完成，共更新 {} 筆", updated);

        // 每筆確認的訂單發布確認事件
        orderIds.forEach(id -> {
            try {
                Order order = orderRepository.findById(id).orElse(null);
                if (order != null) {
                    eventPublisher.publish(new OrderConfirmedEvent(order));
                }
            } catch (Exception e) {
                log.error("[OrderApplicationService] 發布訂單確認事件失敗，orderId={}", id, e);
            }
        });

        return updated;
    }

    /**
     * MyBatis 動態條件搜尋訂單
     *
     * <p>支援多條件組合查詢（狀態、用戶、金額範圍、時間範圍等），
     * 由 MyBatis 動態 SQL 構建，避免 JPA Criteria API 的繁瑣。</p>
     *
     * @param query 搜尋條件 DTO
     * @return      符合條件的訂單摘要列表
     */
    @Transactional(readOnly = true)
    public List<OrderSummary> searchOrders(OrderSearchQuery query) {
        return orderRepository.searchOrders(query);
    }
}

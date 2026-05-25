package com.ecommerce.order.application.factory;

import com.ecommerce.order.domain.exception.BusinessException;
import com.ecommerce.order.domain.factory.OrderFactory;
import com.ecommerce.order.domain.factory.OrderType;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.interfaces.dto.CreateOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 訂閱方案訂單工廠（Factory Pattern - Concrete Factory）
 *
 * <p>負責建立 {@link OrderType#SUBSCRIPTION} 類型的訂單，特性：
 * <ul>
 *   <li><b>自動續約</b>：訂閱預設開啟自動續約，續約日期為建立日起 1 個月後</li>
 *   <li><b>方案驗證</b>：需要有效的 {@code planId}</li>
 *   <li><b>無庫存概念</b>：訂閱為服務性商品</li>
 * </ul>
 * </p>
 *
 * <p><b>擴充說明：</b>未來可注入 PlanFeignClient 向 plan-service
 * 查詢方案價格，目前以 note 欄位記錄方案 ID 供後續處理。</p>
 */
@Slf4j
@Component
public class SubscriptionOrderFactory implements OrderFactory {

    /**
     * 建立訂閱訂單
     *
     * <p>驗證訂閱方案 ID 存在後建立訂單，設定自動續約日期（1 個月後）。
     * 訂閱金額由上游傳入或由方案服務填充。</p>
     *
     * @param req 建立訂單請求（需包含 planId；userId 必填）
     * @return    已初始化的訂閱訂單（狀態 PENDING，含自動續約資訊）
     * @throws BusinessException planId 為空時拋出
     */
    @Override
    public Order create(CreateOrderRequest req) {
        log.debug("[SubscriptionOrderFactory] 建立訂閱訂單，planId={}, userId={}",
                req.getPlanId(), req.getUserId());

        // 驗證方案 ID
        if (req.getPlanId() == null) {
            throw new BusinessException("訂閱方案 ID 不可為空");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime renewalDate = now.plusMonths(1);

        return Order.builder()
                .userId(req.getUserId())
                .orderType(OrderType.SUBSCRIPTION)
                .totalAmount(java.math.BigDecimal.ZERO)     // 實際金額由方案服務填充
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(req.getShippingAddress())
                .note(String.format("訂閱方案 ID：%d，自動續約：%s",
                        req.getPlanId(), renewalDate.toLocalDate()))
                .createdAt(now)
                .build();
    }

    /**
     * 判斷此工廠是否支援指定訂單類型
     *
     * @param type 訂單類型
     * @return     {@code true} 僅當 type 為 {@link OrderType#SUBSCRIPTION}
     */
    @Override
    public boolean supports(OrderType type) {
        return OrderType.SUBSCRIPTION.equals(type);
    }
}

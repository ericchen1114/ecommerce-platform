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
 * 禮品卡訂單工廠（Factory Pattern - Concrete Factory）
 *
 * <p>負責建立 {@link OrderType#GIFT_CARD} 類型的訂單，與標準訂單不同之處：
 * <ul>
 *   <li><b>不驗證庫存</b>：禮品卡為虛擬商品，無庫存概念</li>
 *   <li><b>需要收件人 Email</b>：禮品卡寄送至指定 Email</li>
 *   <li><b>有效期 1 年</b>：禮品卡自建立日起 1 年內有效</li>
 * </ul>
 * </p>
 *
 * <p><b>注意：</b>禮品卡面值（{@code giftCardValue}）即為訂單金額，
 * 不套用會員折扣與地區運費，但仍可能套用稅率。</p>
 */
@Slf4j
@Component
public class GiftCardOrderFactory implements OrderFactory {

    /**
     * 建立禮品卡訂單
     *
     * <p>驗證禮品卡面值與收件人 Email 不為空後建立訂單。
     * 有效期設定為建立時間起 1 年後。</p>
     *
     * @param req 建立訂單請求（需包含 giftCardValue、recipientEmail）
     * @return    已初始化的禮品卡訂單
     * @throws BusinessException 禮品卡面值為空或收件人 Email 為空時拋出
     */
    @Override
    public Order create(CreateOrderRequest req) {
        log.debug("[GiftCardOrderFactory] 建立禮品卡訂單，recipientEmail={}, value={}",
                req.getRecipientEmail(), req.getGiftCardValue());

        // 驗證禮品卡面值
        if (req.getGiftCardValue() == null || req.getGiftCardValue().signum() <= 0) {
            throw new BusinessException("禮品卡面值必須大於 0");
        }

        // 驗證收件人 Email
        if (req.getRecipientEmail() == null || req.getRecipientEmail().isBlank()) {
            throw new BusinessException("禮品卡收件人 Email 不可為空");
        }

        LocalDateTime now = LocalDateTime.now();

        return Order.builder()
                .userId(req.getUserId())
                .orderType(OrderType.GIFT_CARD)
                .totalAmount(req.getGiftCardValue())
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(req.getRecipientEmail())   // 禮品卡以 Email 為寄送目標
                .note("禮品卡有效期至：" + now.plusYears(1).toLocalDate()
                        + "，收件人：" + req.getRecipientEmail())
                .createdAt(now)
                .build();
    }

    /**
     * 判斷此工廠是否支援指定訂單類型
     *
     * @param type 訂單類型
     * @return     {@code true} 僅當 type 為 {@link OrderType#GIFT_CARD}
     */
    @Override
    public boolean supports(OrderType type) {
        return OrderType.GIFT_CARD.equals(type);
    }
}

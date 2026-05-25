package com.ecommerce.order.application.factory;

import com.ecommerce.order.domain.exception.BusinessException;
import com.ecommerce.order.domain.factory.OrderFactory;
import com.ecommerce.order.domain.factory.OrderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 訂單工廠選擇器（Factory Pattern - Factory Selector）
 *
 * <p>Spring 自動注入所有 {@link OrderFactory} 的 {@code @Component} 實作，
 * 根據訂單類型選取對應的工廠，實現多型分派。</p>
 *
 * <h3>設計優點</h3>
 * <ul>
 *   <li><b>開閉原則：</b>新增訂單類型只需新增 {@code @Component} 工廠，
 *       此選擇器和 Service 層無需修改</li>
 *   <li><b>自動掃描：</b>透過 Spring DI 的 {@code List<OrderFactory>} 注入，
 *       所有工廠 Bean 自動被發現</li>
 * </ul>
 *
 * <h3>工廠選取流程</h3>
 * <pre>
 * createOrder(STANDARD)
 *   → factories.stream().filter(f -> f.supports(STANDARD))
 *   → StandardOrderFactory
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderFactorySelector {

    /**
     * 所有 {@link OrderFactory} 實作，由 Spring 自動注入
     * （包含 StandardOrderFactory、GiftCardOrderFactory、SubscriptionOrderFactory）
     */
    private final List<OrderFactory> factories;

    /**
     * 根據訂單類型選取對應的工廠
     *
     * <p>遍歷所有工廠並呼叫 {@link OrderFactory#supports(OrderType)} 判斷，
     * 取第一個符合的工廠。若無符合的工廠則拋出例外，
     * 提示開發者需新增對應的工廠實作。</p>
     *
     * @param type 欲建立的訂單類型
     * @return     支援該類型的 {@link OrderFactory} 實作
     * @throws BusinessException 無任何工廠支援此訂單類型時拋出
     */
    public OrderFactory getFactory(OrderType type) {
        log.debug("[OrderFactorySelector] 選取工廠，orderType={}", type);

        return factories.stream()
                .filter(f -> f.supports(type))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        "不支援的訂單類型：" + type + "（" + type.getDescription() + "）"));
    }
}

package com.ecommerce.payment.interfaces.rest;

import com.ecommerce.payment.domain.model.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 付款 REST Controller
 *
 * <p>主要透過 Saga / RabbitMQ 觸發，HTTP API 供手動測試與管理使用。</p>
 */
@Tag(name = "付款管理", description = "付款建立與查詢 API（整合 Saga Choreography 流程）")
@SecurityRequirement(name = "BearerAuth")
@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    /**
     * 手動建立付款記錄（測試用途）
     */
    @Operation(
        summary = "建立付款記錄",
        description = """
                手動建立付款記錄，**正式流程應透過 Saga 事件觸發**（order-service 建立訂單後自動發送）。

                ### Saga 付款流程
                ```
                order-service 建立訂單
                  → PaymentEventListener 發送 RabbitMQ 訊息到 saga.exchange
                  → payment-service Consumer 接收並處理付款
                  → 成功: 發布 PAYMENT_COMPLETED 事件
                  → 失敗: 發布 PAYMENT_FAILED 事件（觸發 Saga 補償）
                ```

                ### 付款狀態流程
                `PENDING` → `SUCCESS` 或 `FAILED` → `REFUNDED`（退款）
                """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                        {
                          "orderId": 1,
                          "amount": 935.39,
                          "paymentMethod": "CREDIT_CARD",
                          "status": "PENDING"
                        }
                        """)
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "付款記錄建立成功",
            content = @Content(schema = @Schema(example = """
                    {
                      "id": 1,
                      "orderId": 1,
                      "amount": 935.39,
                      "status": "PENDING",
                      "createdAt": "2026-05-22T10:30:00"
                    }
                    """)))
    })
    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        log.info("建立付款：orderId={}", payment.getOrderId());
        // TODO: 串接金流服務
        return ResponseEntity.ok(payment);
    }

    /**
     * 健康檢查
     */
    @Operation(summary = "健康檢查", description = "確認 payment-service 是否正常運行")
    @SecurityRequirement(name = "")
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("payment-service is running");
    }
}

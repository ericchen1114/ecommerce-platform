package com.ecommerce.order.interfaces.rest;

import com.ecommerce.order.application.service.OrderApplicationService;
import com.ecommerce.order.domain.model.Order;
import com.ecommerce.order.interfaces.dto.CreateOrderRequest;
import com.ecommerce.order.interfaces.dto.OrderSearchQuery;
import com.ecommerce.order.interfaces.dto.OrderSummary;
import com.ecommerce.order.interfaces.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 訂單 REST Controller
 *
 * <p>所有回應統一使用 {@link Result} 包裝。</p>
 */
@Tag(name = "訂單管理", description = "訂單的建立、查詢、批次確認 API（整合工廠 + 策略 + 觀察者模式）")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderApplicationService orderService;

    /**
     * 建立訂單（支援三種類型）
     */
    @Operation(
        summary = "建立訂單",
        description = """
                支援三種訂單類型（工廠模式），由 `orderType` 欄位決定：

                #### STANDARD（一般商品訂單）
                必填：`productId`、`quantity`
                選填：`region`（地區）、`memberLevel`（會員等級）、`promoCode`（促銷碼）

                #### GIFT_CARD（禮品卡訂單）
                必填：`giftCardValue`（面值）、`recipientEmail`（收件人 Email）

                #### SUBSCRIPTION（訂閱方案）
                必填：`planId`（方案 ID）

                ### 計價流程（策略模式）
                基礎金額 → 會員折扣 → 促銷碼 → 地區運費 → 稅率 → 最終金額

                ### 建立後自動觸發（觀察者模式）
                - PaymentEventListener：發送付款請求至 Saga
                - NotificationEventListener：發送訂單確認通知
                - LoyaltyEventListener：累積會員積分
                """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(name = "STANDARD 訂單（含折扣）", value = """
                            {
                              "orderType": "STANDARD",
                              "productId": 1,
                              "quantity": 2,
                              "region": "NYC",
                              "memberLevel": "GOLD",
                              "member": true,
                              "promoCode": "SUMMER10",
                              "shippingState": "NY",
                              "shippingAddress": "123 Main St, New York, NY 10001"
                            }
                            """),
                    @ExampleObject(name = "GIFT_CARD 訂單", value = """
                            {
                              "orderType": "GIFT_CARD",
                              "giftCardValue": 500,
                              "recipientEmail": "friend@example.com"
                            }
                            """),
                    @ExampleObject(name = "SUBSCRIPTION 訂單", value = """
                            {
                              "orderType": "SUBSCRIPTION",
                              "planId": 3
                            }
                            """)
                }
            )
        )
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "訂單建立成功",
            content = @Content(schema = @Schema(example = """
                    {
                      "code": "0000",
                      "message": "success",
                      "data": {
                        "id": 1,
                        "orderNo": "ORD20260522000001",
                        "orderType": "STANDARD",
                        "totalAmount": 935.39,
                        "status": "PENDING"
                      }
                    }
                    """))),
        @ApiResponse(responseCode = "400", description = "商品不存在 / 庫存不足 / 無效促銷碼",
            content = @Content(schema = @Schema(example = """
                    {"code":"4000","message":"庫存不足，當前庫存：1，請求數量：2"}
                    """)))
    })
    @PostMapping
    public Result<Order> createOrder(
            @Parameter(description = "由 API Gateway 注入的當前用戶 ID", example = "M83729471")
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @Valid @RequestBody CreateOrderRequest req) {
        req.setUserId(userId);
        return Result.ok(orderService.createOrder(req));
    }

    /**
     * 根據 ID 查詢訂單
     */
    @Operation(summary = "查詢訂單", description = "根據訂單資料庫 ID 取得訂單詳情")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查詢成功"),
        @ApiResponse(responseCode = "400", description = "訂單不存在",
            content = @Content(schema = @Schema(example = """
                    {"code":"4000","message":"訂單不存在：999"}
                    """)))
    })
    @GetMapping("/{id}")
    public Result<Order> getById(
            @Parameter(description = "訂單 ID", example = "1")
            @PathVariable Long id) {
        return Result.ok(orderService.getOrderById(id));
    }

    /**
     * 查詢指定會員的所有訂單
     */
    @Operation(summary = "查詢會員訂單", description = "取得指定會員的所有歷史訂單")
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @GetMapping("/user/{userId}")
    public Result<List<Order>> getUserOrders(
            @Parameter(description = "會員 ID", example = "1")
            @PathVariable Long userId) {
        return Result.ok(orderService.getUserOrders(userId));
    }

    /**
     * 批次確認訂單（後台管理）
     */
    @Operation(
        summary = "批次確認訂單",
        description = "後台管理用途，批次將多筆訂單狀態更新為 CONFIRMED（使用 JDBC Template 批次更新）"
    )
    @ApiResponse(responseCode = "200", description = "更新成功，回傳實際更新筆數")
    @PostMapping("/batch-confirm")
    public Result<Integer> batchConfirm(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                content = @Content(examples = @ExampleObject(value = "[1, 2, 3]")))
            @RequestBody List<Long> orderIds) {
        return Result.ok(orderService.batchConfirmOrders(orderIds));
    }

    /**
     * 多條件搜尋訂單
     */
    @Operation(
        summary = "搜尋訂單",
        description = "MyBatis 動態 SQL 多條件查詢，支援狀態、用戶、金額範圍、時間範圍等過濾條件"
    )
    @ApiResponse(responseCode = "200", description = "查詢成功")
    @GetMapping("/search")
    public Result<List<OrderSummary>> search(
            @Parameter(description = "搜尋條件（status, userId, minAmount, maxAmount 等）")
            OrderSearchQuery query) {
        return Result.ok(orderService.searchOrders(query));
    }

    /**
     * 健康檢查
     */
    @Operation(summary = "健康檢查", description = "確認 order-service 是否正常運行")
    @SecurityRequirement(name = "")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.ok("order-service is running");
    }
}

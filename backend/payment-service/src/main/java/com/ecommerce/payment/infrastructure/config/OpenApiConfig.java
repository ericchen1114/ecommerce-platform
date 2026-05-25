package com.ecommerce.payment.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Payment Service OpenAPI（Swagger）設定
 *
 * <p>Swagger UI：{@code http://localhost:8084/swagger-ui/index.html}</p>
 *
 * <h3>Saga 整合說明</h3>
 * <p>付款服務透過 RabbitMQ 接收來自 order-service 的 Saga 事件，
 * 並在處理完成後發布 {@code PAYMENT_COMPLETED} 或 {@code PAYMENT_FAILED} 事件。</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * 設定 Payment Service OpenAPI 文件
     *
     * @return 完整的 {@link OpenAPI} 設定物件
     */
    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildJwtSecurityScheme()));
    }

    private Info buildApiInfo() {
        return new Info()
                .title("Payment Service API")
                .description("""
                        ## 付款服務 API

                        提供訂單付款處理功能，整合 Saga Choreography 流程。

                        ### Saga 付款流程
                        ```
                        order-service → [PAYMENT_REQUEST] → payment-service
                        payment-service → 處理付款
                          成功 → [PAYMENT_COMPLETED] → order-service 確認訂單
                          失敗 → [PAYMENT_FAILED] → order-service 取消訂單 + 補償庫存
                        ```

                        ### 付款狀態
                        - `PENDING`   — 待處理
                        - `SUCCESS`   — 付款成功
                        - `FAILED`    — 付款失敗
                        - `REFUNDED`  — 已退款
                        """)
                .version("1.0.0")
                .contact(new Contact().name("Backend Team").email("backend@ecommerce.com"))
                .license(new License().name("Internal Use Only"));
    }

    private List<Server> buildServers() {
        return List.of(
                new Server().url("http://localhost:8084").description("本機開發"),
                new Server().url("http://localhost:8080").description("透過 API Gateway")
        );
    }

    private SecurityScheme buildJwtSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("請輸入 JWT Token（由 user-service 登入後取得）");
    }
}

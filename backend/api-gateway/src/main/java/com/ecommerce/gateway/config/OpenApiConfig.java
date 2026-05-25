package com.ecommerce.gateway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * API Gateway OpenAPI（Swagger）聚合設定
 *
 * <p>Gateway 作為所有服務的入口，提供統一的 Swagger UI，
 * 可在 UI 中切換各微服務的 API 文件。</p>
 *
 * <h3>存取方式</h3>
 * <ul>
 *   <li>Gateway Swagger UI：{@code http://localhost:8080/swagger-ui/index.html}</li>
 *   <li>切換服務：使用 Swagger UI 右上角的 Select a definition 下拉選單</li>
 * </ul>
 *
 * <h3>各服務直接 Swagger UI</h3>
 * <ul>
 *   <li>user-service：{@code http://localhost:8081/swagger-ui/index.html}</li>
 *   <li>product-service：{@code http://localhost:8082/swagger-ui/index.html}</li>
 *   <li>order-service：{@code http://localhost:8083/swagger-ui/index.html}</li>
 *   <li>payment-service：{@code http://localhost:8084/swagger-ui/index.html}</li>
 *   <li>notification-service：{@code http://localhost:8085/swagger-ui/index.html}</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * Gateway 自身的 OpenAPI 文件（路由說明等）
     *
     * @return Gateway OpenAPI 設定
     */
    @Bean
    public OpenAPI gatewayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce Platform API Gateway")
                        .description("""
                                ## 電商平台 API Gateway

                                統一入口，提供 JWT 驗證、路由轉發、熔斷降級。

                                ### 服務路由對照
                                | 前綴 | 轉發至 | 說明 |
                                |------|--------|------|
                                | `/api/users/**` | user-service:8081 | 會員與認證 |
                                | `/api/products/**` | product-service:8082 | 商品管理 |
                                | `/api/orders/**` | order-service:8083 | 訂單管理 |
                                | `/api/payments/**` | payment-service:8084 | 付款處理 |

                                ### JWT 認證
                                除了登入/註冊路徑，所有 API 皆需在 Header 帶上：
                                ```
                                Authorization: Bearer <token>
                                ```
                                Token 由 `POST /api/users/login` 或 `POST /api/users/register` 取得。

                                ### 會員識別
                                JWT 驗證通過後，Gateway 自動解析 memberNo 並注入：
                                ```
                                X-User-Id: <memberNo>
                                ```
                                """)
                        .version("1.0.0")
                        .contact(new Contact().name("Backend Team").email("backend@ecommerce.com"))
                        .license(new License().name("Internal Use Only")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway（本機）")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("請輸入 JWT Token（由 /api/users/login 取得）")));
    }
}

package com.ecommerce.order.infrastructure.config;

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
 * Order Service OpenAPI（Swagger）設定
 *
 * <p>Swagger UI：{@code http://localhost:8083/swagger-ui/index.html}</p>
 *
 * <h3>整合設計模式說明</h3>
 * <ul>
 *   <li>工廠模式：{@code orderType} 欄位決定使用哪個工廠建立訂單</li>
 *   <li>策略模式：{@code region}、{@code memberLevel}、{@code promoCode} 影響最終金額</li>
 *   <li>觀察者模式：訂單建立後自動觸發付款、通知、積分監聽器</li>
 * </ul>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * 設定 Order Service OpenAPI 文件
     *
     * @return 完整的 {@link OpenAPI} 設定物件
     */
    @Bean
    public OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildJwtSecurityScheme()));
    }

    private Info buildApiInfo() {
        return new Info()
                .title("Order Service API")
                .description("""
                        ## 訂單服務 API

                        提供訂單的建立、查詢、批次確認等功能。
                        支援三種訂單類型（工廠模式）：
                        - `STANDARD` — 一般商品訂單
                        - `GIFT_CARD` — 禮品卡訂單
                        - `SUBSCRIPTION` — 訂閱方案訂單

                        ### 計價策略（策略模式，依序套用）
                        1. 會員折扣（GOLD=85折 / SILVER=90折 / BRONZE=95折）
                        2. 促銷碼（百分比或固定金額）
                        3. 地區運費（NYC +10% / LA +5% / MIDWEST +2%）
                        4. 稅率（CA=8.5% / NY=8.875% / TX=6.25%）

                        ### 訂單號格式
                        `ORD` + `yyyyMMdd` + `6位序號`，例如：`ORD20260522000001`

                        ### 錯誤碼
                        | code | 說明 |
                        |------|------|
                        | 0000 | 成功 |
                        | 4000 | 業務錯誤（庫存不足、無效促銷碼等）|
                        | 4004 | 訂單不存在 |
                        | 9999 | 系統錯誤 |
                        """)
                .version("1.0.0")
                .contact(new Contact().name("Backend Team").email("backend@ecommerce.com"))
                .license(new License().name("Internal Use Only"));
    }

    private List<Server> buildServers() {
        return List.of(
                new Server().url("http://localhost:8083").description("本機開發"),
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

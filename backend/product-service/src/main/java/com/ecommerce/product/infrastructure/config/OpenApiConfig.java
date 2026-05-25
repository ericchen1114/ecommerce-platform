package com.ecommerce.product.infrastructure.config;

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
 * Product Service OpenAPI（Swagger）設定
 *
 * <p>Swagger UI：{@code http://localhost:8082/swagger-ui/index.html}</p>
 *
 * <h3>資料庫說明</h3>
 * <p>商品資料儲存於 MongoDB，支援彈性 schema 與全文搜尋。</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * 設定 Product Service OpenAPI 文件
     *
     * @return 完整的 {@link OpenAPI} 設定物件
     */
    @Bean
    public OpenAPI productServiceOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildJwtSecurityScheme()));
    }

    private Info buildApiInfo() {
        return new Info()
                .title("Product Service API")
                .description("""
                        ## 商品服務 API

                        提供商品的查詢、新增、刪除等功能。
                        商品資料儲存於 MongoDB，支援分類查詢與關鍵字搜尋。

                        ### 快取機制
                        熱門商品資料透過 Redis 快取，TTL = 10 分鐘。

                        ### 搜尋支援
                        整合 Elasticsearch 提供全文搜尋功能。
                        """)
                .version("1.0.0")
                .contact(new Contact().name("Backend Team").email("backend@ecommerce.com"))
                .license(new License().name("Internal Use Only"));
    }

    private List<Server> buildServers() {
        return List.of(
                new Server().url("http://localhost:8082").description("本機開發"),
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

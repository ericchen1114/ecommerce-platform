package com.ecommerce.user.infrastructure.config;

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
 * User Service OpenAPI（Swagger）設定
 *
 * <p>啟動後可透過以下路徑存取 Swagger UI：
 * <ul>
 *   <li>Swagger UI：{@code http://localhost:8081/swagger-ui/index.html}</li>
 *   <li>OpenAPI JSON：{@code http://localhost:8081/v3/api-docs}</li>
 * </ul>
 * </p>
 *
 * <h3>JWT 驗證說明</h3>
 * <p>需要驗證的 API 請在 Swagger UI 右上角點擊「Authorize」，
 * 輸入 {@code Bearer <token>}（包含 Bearer 前綴）。</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * 設定 OpenAPI 文件基本資訊、伺服器清單與 JWT Bearer 驗證方案
     *
     * @return 完整的 {@link OpenAPI} 設定物件
     */
    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildJwtSecurityScheme()));
    }

    /**
     * API 基本資訊
     */
    private Info buildApiInfo() {
        return new Info()
                .title("User Service API")
                .description("""
                        ## 會員服務 API

                        提供會員註冊、登入、身份驗證等功能。

                        ### 登入流程（步驟式）
                        1. `POST /api/users/check` — 判斷手機號或 Email 是否已是會員
                        2. （已是會員）`POST /api/users/login` — 輸入密碼取得 JWT Token
                        3. （非會員）`POST /api/users/register` — 填寫資料完成註冊，自動取得 JWT Token

                        ### 錯誤碼說明
                        | code | 說明 |
                        |------|------|
                        | 0000 | 成功 |
                        | 1001 | 帳號或密碼錯誤 |
                        | 1002 | 手機號已存在 |
                        | 1003 | Email 已存在 |
                        | 4000 | 業務邏輯錯誤 |
                        | 4001 | 未授權 |
                        | 9999 | 系統錯誤 |
                        """)
                .version("1.0.0")
                .contact(new Contact()
                        .name("Backend Team")
                        .email("backend@ecommerce.com"))
                .license(new License()
                        .name("Internal Use Only"));
    }

    /**
     * 伺服器清單（本機開發 + 生產環境）
     */
    private List<Server> buildServers() {
        return List.of(
                new Server().url("http://localhost:8081").description("本機開發"),
                new Server().url("http://localhost:8080").description("透過 API Gateway")
        );
    }

    /**
     * JWT Bearer Token 驗證方案
     */
    private SecurityScheme buildJwtSecurityScheme() {
        return new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("請輸入 JWT Token（不需加 Bearer 前綴，Swagger 會自動加上）");
    }
}

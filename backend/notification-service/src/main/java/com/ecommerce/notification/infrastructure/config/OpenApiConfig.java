package com.ecommerce.notification.infrastructure.config;

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
 * Notification Service OpenAPI（Swagger）設定
 *
 * <p>Swagger UI：{@code http://localhost:8085/swagger-ui/index.html}</p>
 *
 * <h3>通知管道說明</h3>
 * <p>通知服務透過 RabbitMQ 接收事件並發送通知，
 * 支援 Email 和 SMS 兩種管道，採用非同步處理。</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    /**
     * 設定 Notification Service OpenAPI 文件
     *
     * @return 完整的 {@link OpenAPI} 設定物件
     */
    @Bean
    public OpenAPI notificationServiceOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, buildJwtSecurityScheme()));
    }

    private Info buildApiInfo() {
        return new Info()
                .title("Notification Service API")
                .description("""
                        ## 通知服務 API

                        透過 RabbitMQ 接收其他服務的事件，非同步發送 Email 與 SMS 通知。

                        ### 支援的通知類型
                        | 事件 | Email | SMS |
                        |------|-------|-----|
                        | 訂單建立 | ✅ | ✅ |
                        | 訂單確認 | ✅ | ✅ |
                        | 付款成功 | ✅ | ✅ |
                        | 付款失敗 | ✅ | ❌ |

                        ### RabbitMQ Exchange
                        - `notification.exchange`（direct）
                        - Routing keys：`email.order.created`、`sms.order.created` 等
                        """)
                .version("1.0.0")
                .contact(new Contact().name("Backend Team").email("backend@ecommerce.com"))
                .license(new License().name("Internal Use Only"));
    }

    private List<Server> buildServers() {
        return List.of(
                new Server().url("http://localhost:8085").description("本機開發"),
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

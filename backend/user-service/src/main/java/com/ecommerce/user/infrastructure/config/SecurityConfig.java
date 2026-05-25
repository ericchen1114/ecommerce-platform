package com.ecommerce.user.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 設定
 *
 * <p>採用 Stateless JWT 驗證，無 Session。
 * API Gateway 已處理 JWT 驗證，此設定主要用於本服務直接存取的安全控制。</p>
 *
 * <h3>公開端點（無需 JWT）</h3>
 * <ul>
 *   <li>{@code /api/users/check}    - 確認是否為會員</li>
 *   <li>{@code /api/users/login}    - 登入</li>
 *   <li>{@code /api/users/register} - 註冊</li>
 *   <li>{@code /api/users/health}   - 健康檢查</li>
 *   <li>{@code /v3/api-docs/**}     - OpenAPI JSON（Swagger）</li>
 *   <li>{@code /swagger-ui/**}      - Swagger UI 靜態資源</li>
 *   <li>{@code /actuator/**}        - Spring Actuator</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    /** 公開端點（不需 JWT） */
    private static final String[] PUBLIC_PATHS = {
            "/api/users/check",
            "/api/users/login",
            "/api/users/register",
            "/api/users/health",
            "/actuator/**",
            // ─── Swagger / OpenAPI 路徑 ───────────────────────────────────────
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui/index.html",
            "/swagger-resources/**",
            "/webjars/**"
    };

    /**
     * 設定安全過濾鏈
     *
     * <p>停用 CSRF（Stateless API 不需要），
     * 設定 Session 為 STATELESS，公開路徑不需驗證。</p>
     *
     * @param http {@link HttpSecurity} 設定物件
     * @return     建立好的 {@link SecurityFilterChain}
     * @throws Exception 設定錯誤時拋出
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    /**
     * BCrypt 密碼編碼器
     *
     * @return {@link BCryptPasswordEncoder}（預設 cost factor = 10）
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

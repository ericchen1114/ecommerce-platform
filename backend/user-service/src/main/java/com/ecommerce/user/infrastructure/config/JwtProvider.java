package com.ecommerce.user.infrastructure.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 產生器
 *
 * <p>使用 HMAC-SHA256 演算法簽發 JWT Token，
 * 金鑰與過期時間由 {@code application.yml} 注入。</p>
 *
 * <p><b>Token 結構：</b>
 * <ul>
 *   <li>Subject：會員編號（memberNo，例如 M83729471）</li>
 *   <li>Claims：{@code role}（USER / ADMIN）</li>
 *   <li>IssuedAt：簽發時間</li>
 *   <li>Expiration：到期時間（預設 86400000ms = 24 小時）</li>
 * </ul>
 * </p>
 */
@Component
public class JwtProvider {

    /** JWT 簽名金鑰（至少 256 bit），從 application.yml 注入 */
    @Value("${jwt.secret}")
    private String secret;

    /** Token 有效毫秒數，從 application.yml 注入 */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 產生 JWT Token
     *
     * <p>Subject 設為 {@code memberNo}（對外會員編號），
     * 讓 API Gateway 可直接從 Token 取得會員識別符，
     * 不暴露 DB 內部自增 id。</p>
     *
     * @param memberNo 對外會員編號（例如 M83729471），作為 JWT Subject
     * @param role     會員角色字串（{@code "USER"} 或 {@code "ADMIN"}）
     * @return         已簽名的 JWT Token 字串
     */
    public String generate(String memberNo, String role) {
        return Jwts.builder()
                .subject(memberNo)
                .claims(Map.of("role", role))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}

package com.ecommerce.user.infrastructure.config;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

/**
 * 會員編號產生器
 *
 * <p>格式：{@code M} + 8 位隨機數字，例如 {@code M83729471}</p>
 *
 * <p><b>設計原則：</b>
 * <ul>
 *   <li>直接存入 DB VARCHAR 欄位，不做任何編解碼</li>
 *   <li>SQL 可直接 {@code WHERE member_no = 'M83729471'} 查詢</li>
 *   <li>{@link SecureRandom} 使用 OS 層級的硬體熵源，不可預測</li>
 *   <li>8 位數字提供 9,000 萬種可能，碰撞概率極低</li>
 *   <li>DB {@code UNIQUE} 約束為最後防線；{@link com.ecommerce.user.application.service.AuthApplicationService} 最多重試 5 次</li>
 * </ul>
 * </p>
 *
 * <p><b>碰撞概率估算（生日悖論）：</b>
 * <ul>
 *   <li>10 萬會員時：約 0.055%（加上重試機制可完全忽略）</li>
 *   <li>100 萬會員時：約 5.4%（此時建議改為 9 位）</li>
 * </ul>
 * </p>
 */
@Component
public class MemberNoGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    /** 隨機數字位數 */
    private static final int DIGITS = 8;

    /** 最小值：10,000,000 */
    private static final int MIN = (int) Math.pow(10, DIGITS - 1);

    /** 最大值：99,999,999 */
    private static final int MAX = (int) Math.pow(10, DIGITS) - 1;

    /**
     * 產生一組隨機會員編號
     *
     * <p>使用 {@link SecureRandom#nextInt(int)} 在 [MIN, MAX] 範圍內產生均勻分布的隨機數，
     * 加上固定前綴 {@code M} 組成最終編號。</p>
     *
     * <p><b>注意：</b>此方法不保證全局唯一，呼叫方應搭配 DB UNIQUE 約束與重試邏輯。</p>
     *
     * @return 格式為 {@code M} + 8 位數字的會員編號，例如 {@code M83729471}
     */
    public String generate() {
        int num = MIN + RANDOM.nextInt(MAX - MIN + 1);
        return "M" + num;
    }
}

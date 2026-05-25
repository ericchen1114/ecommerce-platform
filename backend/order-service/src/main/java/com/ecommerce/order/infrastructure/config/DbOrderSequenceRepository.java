package com.ecommerce.order.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

/**
 * 訂單序號 DB 備援儲存庫
 *
 * <p>當 Redis 不可用時，使用此儲存庫以 MySQL {@code LAST_INSERT_ID()} 技巧
 * 實現原子遞增，效果等同 Redis INCR，無需應用層額外加鎖。</p>
 *
 * <p><b>原子性保證：</b><br>
 * {@code ON DUPLICATE KEY UPDATE seq_value = LAST_INSERT_ID(seq_value + 1)}
 * 在 MySQL InnoDB 執行時會對目標行加排他鎖（X Lock），
 * 所有並發寫入依序排隊執行，每個連線的 {@code LAST_INSERT_ID()} 互不干擾。</p>
 *
 * <p><b>依賴的 DB Table：</b>
 * <pre>{@code
 * CREATE TABLE order_sequence (
 *     seq_date  DATE   NOT NULL,
 *     seq_value BIGINT NOT NULL DEFAULT 0,
 *     PRIMARY KEY (seq_date)
 * );
 * }</pre>
 * </p>
 */
@Repository
@RequiredArgsConstructor
public class DbOrderSequenceRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String UPSERT_SQL = """
            INSERT INTO order_sequence (seq_date, seq_value)
            VALUES (?, 1)
            ON DUPLICATE KEY UPDATE
                seq_value = LAST_INSERT_ID(seq_value + 1)
            """;

    private static final String CURRENT_SQL =
            "SELECT COALESCE(seq_value, 0) FROM order_sequence WHERE seq_date = ?";

    /**
     * 原子遞增並取得當天下一個序號
     *
     * <p>若當天記錄不存在則插入並從 1 開始；已存在則遞增後返回。
     * 並發下每個呼叫拿到的序號保證不重複。</p>
     *
     * @param date 要取序號的日期（通常為今天 {@code LocalDate.now()}）
     * @return     當天的下一個序號（從 1 開始遞增）
     * @throws IllegalStateException {@code LAST_INSERT_ID()} 回傳異常值時拋出
     */
    public long nextSeq(LocalDate date) {
        jdbcTemplate.update(UPSERT_SQL, date);
        Long seq = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
        if (seq == null || seq <= 0) {
            throw new IllegalStateException("DB 序號產生失敗");
        }
        return seq;
    }

    /**
     * 查詢當天 DB 已累積到的最大序號（唯讀，不遞增）
     *
     * <p>主要用於 Redis 恢復後的同步起點：
     * 讀取 DB 當天已到哪個序號，讓 Redis 從該值繼續，
     * 避免 Redis 恢復後從 1 開始造成重複。</p>
     *
     * @param date 要查詢的日期
     * @return     當天已用的最大序號；若當天無任何記錄則返回 {@code 0}
     */
    public long currentSeq(LocalDate date) {
        try {
            Long seq = jdbcTemplate.queryForObject(CURRENT_SQL, Long.class, date);
            return seq != null ? seq : 0L;
        } catch (Exception e) {
            return 0L;
        }
    }
}

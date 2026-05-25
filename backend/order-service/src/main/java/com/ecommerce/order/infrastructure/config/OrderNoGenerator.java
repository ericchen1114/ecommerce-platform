package com.ecommerce.order.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 訂單號產生器
 *
 * 格式：ORD + yyyyMMdd + 6位補零流水號，例如 ORD20260522000001
 *
 * ┌─────────────────────────────────────────────────────────┐
 * │  正常模式：Redis INCR（原子遞增，零碰撞）               │
 * │                                                         │
 * │  Redis 故障：自動切 DB 備援（MySQL 行鎖原子遞增）       │
 * │                                                         │
 * │  Redis 恢復：從 DB 當天已用序號繼續，不從 1 重來        │
 * │             （分散式 SETNX 鎖保護初始化，避免競態）     │
 * └─────────────────────────────────────────────────────────┘
 *
 * Redis 恢復時的競態問題與解法：
 *
 *   問題：
 *     Redis 掛掉期間 DB 生成序號 1~50
 *     Redis 恢復後 key 消失，多個請求同時看到 key 不存在
 *     若各自直接 INCR，都會從 1 開始 → 與 DB 的 1~50 重複
 *
 *   解法：SETNX 分散式鎖
 *     ① 只有第一個拿到鎖的執行緒查 DB 並初始化 Redis key
 *     ② 其餘執行緒等待 key 建立完成
 *     ③ 所有執行緒都等 key 就緒後再 INCR
 *     → Redis 從 DB 的 50 繼續：51, 52, 53... 完全無重複
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class OrderNoGenerator {

    private static final DateTimeFormatter DATE_FMT  = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String KEY_PREFIX  = "order:no:";
    private static final String LOCK_SUFFIX = ":init_lock";
    private static final int    SEQ_DIGITS  = 6;

    /** Redis key 保留 2 天，SETNX 鎖 TTL 5 秒（防止鎖持有者崩潰造成死鎖）*/
    private static final long KEY_TTL_DAYS  = 2;
    private static final long LOCK_TTL_SEC  = 5;

    /** 等待其他執行緒初始化的間隔與最大次數（最多等 500ms）*/
    private static final int WAIT_INTERVAL_MS = 10;
    private static final int WAIT_MAX_TIMES   = 50;

    private final StringRedisTemplate       redisTemplate;
    private final DbOrderSequenceRepository dbSequenceRepo;

    // ─────────────────────────────────────────────────────────────
    // 公開方法
    // ─────────────────────────────────────────────────────────────

    public String generate() {
        LocalDate today   = LocalDate.now();
        String    dateStr = today.format(DATE_FMT);
        long      seq     = generateSeq(today, dateStr);
        return "ORD" + dateStr + String.format("%0" + SEQ_DIGITS + "d", seq);
    }

    // ─────────────────────────────────────────────────────────────
    // 內部：序號取得流程
    // ─────────────────────────────────────────────────────────────

    private long generateSeq(LocalDate today, String dateStr) {
        try {
            return redisIncr(today, dateStr);
        } catch (Exception redisEx) {
            log.warn("[訂單號] Redis 不可用（{}），切換至 DB 備援", redisEx.getMessage());
            try {
                return dbSequenceRepo.nextSeq(today);
            } catch (Exception dbEx) {
                log.error("[訂單號] Redis 與 DB 備援皆失敗", dbEx);
                throw new IllegalStateException("訂單號產生失敗：Redis 與 DB 皆不可用", dbEx);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Redis INCR（含 key 初始化 + 恢復同步）
    // ─────────────────────────────────────────────────────────────

    private long redisIncr(LocalDate today, String dateStr) {
        String seqKey  = KEY_PREFIX + dateStr;
        String lockKey = seqKey + LOCK_SUFFIX;

        // Key 不存在時才需要初始化（正常情況下 key 已存在，直接 INCR）
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(seqKey))) {
            ensureKeyInitialized(seqKey, lockKey, today);
        }

        Long seq = redisTemplate.opsForValue().increment(seqKey);
        if (seq == null) throw new IllegalStateException("Redis INCR 回傳 null");

        if (seq > 999_999) {
            log.warn("[訂單號] 當日序號超過 999999（seq={}），請考慮擴充位數", seq);
        }
        return seq;
    }

    /**
     * 確保 Redis key 正確初始化
     *
     * 競態保護：
     *   用 SETNX 搶鎖，只有一個執行緒負責初始化。
     *   其餘執行緒自旋等待 key 出現，再繼續 INCR。
     */
    private void ensureKeyInitialized(String seqKey, String lockKey, LocalDate today) {

        // 嘗試搶鎖（SETNX + TTL）
        Boolean gotLock = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", LOCK_TTL_SEC, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(gotLock)) {
            // ── 搶到鎖：負責初始化 ───────────────────────────────
            try {
                // 再次確認（double-check）：可能其他執行緒已初始化完畢
                if (Boolean.TRUE.equals(redisTemplate.hasKey(seqKey))) return;

                // 查 DB 當天已用序號
                long dbSeq = dbSequenceRepo.currentSeq(today);

                if (dbSeq > 0) {
                    // ── Redis 恢復情境：從 DB 序號繼續 ───────────
                    // 將 key 設定為 dbSeq，之後第一個 INCR 會得到 dbSeq+1
                    redisTemplate.opsForValue().set(seqKey, String.valueOf(dbSeq));
                    log.warn("[訂單號] Redis 恢復同步完成：DB seq={}, Redis 將從 {} 繼續",
                            dbSeq, dbSeq + 1);
                } else {
                    // ── 全新一天（或 Redis 從未掛過）：從 0 開始 ─
                    // 設為 0，之後第一個 INCR 得到 1
                    redisTemplate.opsForValue().set(seqKey, "0");
                    log.info("[訂單號] 當日 Redis key 初始化完成（全新一天）");
                }

                redisTemplate.expire(seqKey, KEY_TTL_DAYS, TimeUnit.DAYS);

            } finally {
                // 無論如何都釋放鎖
                redisTemplate.delete(lockKey);
            }

        } else {
            // ── 未搶到鎖：等待初始化執行緒完成 ──────────────────
            waitForKeyReady(seqKey);
        }
    }

    /**
     * 自旋等待 key 就緒（最多 500ms）
     * 通常幾十毫秒內完成，極少需要等滿 500ms
     */
    private void waitForKeyReady(String seqKey) {
        for (int i = 0; i < WAIT_MAX_TIMES; i++) {
            if (Boolean.TRUE.equals(redisTemplate.hasKey(seqKey))) {
                return;  // key 就緒，可以繼續 INCR
            }
            try {
                Thread.sleep(WAIT_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("等待 Redis key 初始化時被中斷");
            }
        }
        // 超時：500ms 內 key 仍未出現（初始化執行緒可能崩潰）
        // 讓鎖 TTL 自然過期後下一個請求重試，這裡拋例外觸發 DB 備援
        throw new IllegalStateException("等待 Redis key 初始化超時（500ms）");
    }
}

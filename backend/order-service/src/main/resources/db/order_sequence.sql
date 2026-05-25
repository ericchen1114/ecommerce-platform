-- 訂單序號備援表（Redis 故障時使用）
-- 每天一筆記錄，seq_value 由 MySQL LAST_INSERT_ID() 原子遞增
CREATE TABLE IF NOT EXISTS order_sequence (
    seq_date  DATE         NOT NULL,
    seq_value BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (seq_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 設定 members 表 AUTO_INCREMENT 起始值為 100000
-- 讓對外顯示的 Hashids 不從 1 開始，進一步隱藏規模
-- 注意：JPA ddl-auto: update 建表後才能執行此語句
ALTER TABLE members AUTO_INCREMENT = 100000;

package com.ecommerce.user.application.command;

import lombok.Value;

/**
 * 會員登入 Command（CQRS Write Side）
 *
 * <p>由 {@code AuthController} 從 {@code LoginRequest} 轉換而來，
 * 傳遞給 {@code AuthApplicationService#login} 執行驗證。</p>
 */
@Value
public class LoginCommand {

    /**
     * 登入識別符：手機號（09XXXXXXXX）或 Email
     * Application Service 根據格式自動判斷查詢欄位
     */
    String identifier;

    /** 明碼密碼，由 Application Service 與 DB 雜湊值比對 */
    String password;
}

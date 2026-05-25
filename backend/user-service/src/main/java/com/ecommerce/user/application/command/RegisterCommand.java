package com.ecommerce.user.application.command;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDate;

/**
 * 會員註冊 Command（CQRS Write Side）
 *
 * <p>由 {@code AuthController} 從 {@code RegisterRequest} 轉換而來，
 * 傳遞給 {@code AuthApplicationService} 執行業務邏輯。</p>
 *
 * <p>使用 Lombok {@link Value} 確保物件不可變（immutable），
 * 防止 Command 在傳遞過程中被意外修改。</p>
 */
@Value
@Builder
public class RegisterCommand {

    /** 真實姓名 */
    String    fullName;

    /** 手機號（格式：09XXXXXXXX），作為主要登入識別符 */
    String    phone;

    /** Email，可為 {@code null}（選填）*/
    String    email;

    /** 出生年月日 */
    LocalDate birthday;

    /** 聯絡地址 */
    String    address;

    /** 明碼密碼，由 Application Service 負責加密，Command 本身不做加密 */
    String    password;
}

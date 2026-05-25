package com.ecommerce.user.interfaces.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * 統一 API 回應包裝物件
 *
 * <p>所有 API 回應皆使用此物件包裝，格式如下：
 * <pre>{@code
 * {
 *   "code":    "0000",
 *   "message": "success",
 *   "data":    { ... }
 * }
 * }</pre>
 * </p>
 *
 * <p><b>錯誤碼規範：</b>
 * <ul>
 *   <li>{@code 0000} - 成功</li>
 *   <li>{@code 1001} - 帳號或密碼錯誤</li>
 *   <li>{@code 1002} - 手機號已存在</li>
 *   <li>{@code 1003} - Email 已存在</li>
 *   <li>{@code 1004} - 表單驗證失敗</li>
 *   <li>{@code 4000} - 一般業務邏輯錯誤</li>
 *   <li>{@code 4001} - 未授權</li>
 *   <li>{@code 9999} - 系統內部錯誤</li>
 * </ul>
 * </p>
 *
 * @param <T> 回應資料的型別
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /** 業務狀態碼，{@code "0000"} 表示成功 */
    private final String code;

    /** 說明訊息，成功時為 {@code "success"} */
    private final String message;

    /** 回應資料，失敗時為 {@code null}（序列化時省略） */
    private final T data;

    /**
     * 私有建構子，統一由工廠方法建立
     *
     * @param code    業務狀態碼
     * @param message 說明訊息
     * @param data    回應資料
     */
    private Result(String code, String message, T data) {
        this.code    = code;
        this.message = message;
        this.data    = data;
    }

    /**
     * 成功回應（含資料）
     *
     * @param data 要回傳的資料
     * @param <T>  資料型別
     * @return     code=0000 的成功 Result
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>("0000", "success", data);
    }

    /**
     * 成功回應（無資料）
     *
     * @param <T> 資料型別（實際為 Void）
     * @return    code=0000 的成功 Result，data 為 null
     */
    public static <T> Result<T> ok() {
        return new Result<>("0000", "success", null);
    }

    /**
     * 失敗回應
     *
     * @param code    業務錯誤碼
     * @param message 對外顯示的錯誤說明
     * @param <T>     資料型別
     * @return        指定錯誤碼的失敗 Result
     */
    public static <T> Result<T> fail(String code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 一般請求錯誤（HTTP 400）
     *
     * @param message 錯誤說明
     * @param <T>     資料型別
     * @return        code=4000 的失敗 Result
     */
    public static <T> Result<T> badRequest(String message) {
        return fail("4000", message);
    }

    /**
     * 未授權錯誤（HTTP 401）
     *
     * @param message 錯誤說明
     * @param <T>     資料型別
     * @return        code=4001 的失敗 Result
     */
    public static <T> Result<T> unauthorized(String message) {
        return fail("4001", message);
    }

    /**
     * 系統內部錯誤（HTTP 500）
     *
     * <p>不對外暴露內部錯誤細節，統一回傳固定訊息。</p>
     *
     * @param <T> 資料型別
     * @return    code=9999 的失敗 Result
     */
    public static <T> Result<T> serverError() {
        return fail("9999", "系統發生錯誤，請稍後再試");
    }
}

package com.ecommerce.user.interfaces.rest;

import com.ecommerce.user.interfaces.dto.Result;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全域例外處理器
 *
 * <p>攔截所有 Controller 層拋出的例外，統一轉換為 {@link Result} 格式回應，
 * 確保 API 回應結構一致，前端只需處理一種格式。</p>
 *
 * <p><b>錯誤碼對照：</b>
 * <ul>
 *   <li>{@code 1001} - 帳號或密碼錯誤</li>
 *   <li>{@code 1002} - 手機號已存在</li>
 *   <li>{@code 1003} - Email 已存在</li>
 *   <li>{@code 1004} - 表單驗證失敗</li>
 *   <li>{@code 4000} - 一般業務邏輯錯誤</li>
 *   <li>{@code 9999} - 系統內部錯誤</li>
 * </ul>
 * </p>
 */
@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理業務邏輯例外（{@link IllegalArgumentException}）
     *
     * <p>業務例外訊息格式為 {@code "描述（code:XXXX）"}，
     * 此方法會解析尾部的錯誤碼並去除後再回傳。</p>
     *
     * @param ex 業務邏輯例外
     * @return   HTTP 400 + {@link Result}（含解析出的業務錯誤碼）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Result<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("業務邏輯錯誤: {}", ex.getMessage());
        String code = extractCode(ex.getMessage(), "4000");
        String msg  = removeSuffix(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail(code, msg));
    }

    /**
     * 處理 Bean Validation 表單驗證失敗（{@link MethodArgumentNotValidException}）
     *
     * <p>將所有欄位錯誤合併為一個訊息字串，以「；」分隔。</p>
     *
     * @param ex 驗證失敗例外，包含所有欄位錯誤明細
     * @return   HTTP 400 + {@link Result}（code: 1004）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + "：" + e.getDefaultMessage())
                .collect(Collectors.joining("；"));
        log.warn("參數驗證失敗: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Result.fail("1004", errors));
    }

    /**
     * 處理所有未預期的系統例外
     *
     * <p>記錄完整 stack trace，回傳統一的系統錯誤訊息（不洩漏內部細節）。</p>
     *
     * @param ex 任何未被其他 handler 攔截的例外
     * @return   HTTP 500 + {@link Result}（code: 9999）
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleGeneral(Exception ex) {
        log.error("未預期錯誤: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.serverError());
    }

    /**
     * 從訊息尾部解析內嵌的業務錯誤碼
     *
     * <p>例如 {@code "帳號或密碼錯誤（code:1001）"} → {@code "1001"}</p>
     *
     * @param message     例外訊息字串
     * @param defaultCode 解析失敗時的預設錯誤碼
     * @return            解析出的錯誤碼，或 {@code defaultCode}
     */
    private String extractCode(String message, String defaultCode) {
        if (message == null) return defaultCode;
        int start = message.lastIndexOf("（code:");
        int end   = message.lastIndexOf("）");
        if (start >= 0 && end > start) {
            return message.substring(start + 6, end).trim();
        }
        return defaultCode;
    }

    /**
     * 移除訊息尾部的錯誤碼標記，只保留可對外顯示的說明文字
     *
     * <p>例如 {@code "帳號或密碼錯誤（code:1001）"} → {@code "帳號或密碼錯誤"}</p>
     *
     * @param message 原始例外訊息
     * @return        去除錯誤碼後的純說明文字
     */
    private String removeSuffix(String message) {
        if (message == null) return "";
        int start = message.lastIndexOf("（code:");
        return start >= 0 ? message.substring(0, start).trim() : message;
    }
}

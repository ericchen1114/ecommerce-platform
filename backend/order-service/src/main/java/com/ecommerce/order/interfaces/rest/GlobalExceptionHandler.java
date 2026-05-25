package com.ecommerce.order.interfaces.rest;

import com.ecommerce.order.domain.exception.BusinessException;
import com.ecommerce.order.interfaces.dto.Result;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全域例外處理器
 *
 * <p>統一捕獲所有 Controller 拋出的例外，並轉換為 {@link Result} 格式回傳，
 * 確保 API 回應格式一致。</p>
 *
 * <h3>例外對應規則</h3>
 * <ul>
 *   <li>{@link BusinessException}                → HTTP 400 / code=4000（業務邏輯錯誤）</li>
 *   <li>{@link IllegalArgumentException}         → HTTP 400 / code=4000（參數錯誤）</li>
 *   <li>{@link MethodArgumentNotValidException}  → HTTP 400 / code=4000（表單驗證失敗）</li>
 *   <li>{@link Exception}                        → HTTP 500 / code=9999（系統錯誤）</li>
 * </ul>
 */
@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理業務邏輯例外（商品不存在、庫存不足、無效促銷碼等）
     *
     * <p>由工廠、策略等設計模式中的業務驗證拋出，
     * 對應到 code=4000 的錯誤回應。</p>
     *
     * @param ex {@link BusinessException} 例外
     * @return   HTTP 400 + code=4000 的 Result
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException ex) {
        log.warn("[GlobalExceptionHandler] 業務邏輯錯誤：{}", ex.getMessage());
        return Result.badRequest(ex.getMessage());
    }

    /**
     * 處理參數錯誤例外（訂單不存在等）
     *
     * @param ex {@link IllegalArgumentException} 例外
     * @return   HTTP 400 + code=4000 的 Result
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("[GlobalExceptionHandler] 參數錯誤：{}", ex.getMessage());
        return Result.badRequest(ex.getMessage());
    }

    /**
     * 處理 Bean Validation 驗證失敗（{@code @Valid} 標注的欄位驗證不通過）
     *
     * @param ex {@link MethodArgumentNotValidException} 例外
     * @return   HTTP 400 + code=4000 的 Result（包含所有欄位錯誤訊息）
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("；"));
        log.warn("[GlobalExceptionHandler] 表單驗證失敗：{}", errors);
        return Result.badRequest(errors);
    }

    /**
     * 處理所有未預期的系統例外
     *
     * <p>不對外暴露錯誤細節，統一回傳「系統發生錯誤，請稍後再試」，
     * 詳細錯誤記錄於日誌中。</p>
     *
     * @param ex 未預期例外
     * @return   HTTP 500 + code=9999 的 Result
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Result<Void> handleGeneral(Exception ex) {
        log.error("[GlobalExceptionHandler] 系統錯誤：{}", ex.getMessage(), ex);
        return Result.serverError();
    }
}

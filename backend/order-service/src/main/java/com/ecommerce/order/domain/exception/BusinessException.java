package com.ecommerce.order.domain.exception;

/**
 * 業務邏輯例外
 *
 * <p>當業務規則違反時（如商品不存在、庫存不足、無效促銷碼等）拋出此例外，
 * 由 {@code GlobalExceptionHandler} 統一捕獲並回傳對應的錯誤回應。</p>
 *
 * <p>此例外屬於 Domain 層，不依賴任何框架，為純 Java 類別。</p>
 */
public class BusinessException extends RuntimeException {

    /**
     * 以訊息建立例外
     *
     * @param message 對外說明的錯誤訊息
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * 以訊息與原因建立例外
     *
     * @param message 對外說明的錯誤訊息
     * @param cause   原始例外（用於日誌追蹤）
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}

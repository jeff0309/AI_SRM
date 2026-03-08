package com.gsrm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 資源不存在例外.
 *
 * <p>當請求的資源（Entity）在資料庫中找不到時拋出此例外，
 * 對應 HTTP 404 Not Found 狀態碼。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 建立資源不存在例外.
     *
     * @param message 錯誤訊息
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 建立資源不存在例外（附帶原因）.
     *
     * @param message 錯誤訊息
     * @param cause   原始例外
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 建立資源不存在例外（使用資源類型與 ID）.
     *
     * @param resourceType 資源類型名稱
     * @param id           資源 ID
     */
    public ResourceNotFoundException(String resourceType, Long id) {
        super("找不到 " + resourceType + "，ID: " + id);
    }

    /**
     * 建立資源不存在例外（使用資源類型與欄位值）.
     *
     * @param resourceType 資源類型名稱
     * @param fieldName    欄位名稱
     * @param fieldValue   欄位值
     */
    public ResourceNotFoundException(String resourceType, String fieldName, Object fieldValue) {
        super("找不到 " + resourceType + "，" + fieldName + " = " + fieldValue);
    }
}

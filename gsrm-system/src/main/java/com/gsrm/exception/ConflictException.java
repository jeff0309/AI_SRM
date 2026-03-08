package com.gsrm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 資源衝突例外.
 *
 * <p>當嘗試建立的資源已存在（如重複的使用者名稱、地面站名稱）時拋出此例外，
 * 對應 HTTP 409 Conflict 狀態碼。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends RuntimeException {

    /**
     * 建立資源衝突例外.
     *
     * @param message 錯誤訊息
     */
    public ConflictException(String message) {
        super(message);
    }

    /**
     * 建立資源衝突例外（附帶原因）.
     *
     * @param message 錯誤訊息
     * @param cause   原始例外
     */
    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 建立資源已存在衝突例外.
     *
     * @param resourceType 資源類型
     * @param fieldName    欄位名稱
     * @param fieldValue   欄位值
     */
    public ConflictException(String resourceType, String fieldName, Object fieldValue) {
        super(resourceType + " 已存在：" + fieldName + " = " + fieldValue);
    }
}

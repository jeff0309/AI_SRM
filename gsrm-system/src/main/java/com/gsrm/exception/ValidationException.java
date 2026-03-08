package com.gsrm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 資料驗證例外.
 *
 * <p>當輸入資料不符合業務規則時（超出程式碼層驗證）拋出此例外，
 * 對應 HTTP 400 Bad Request 狀態碼。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    /**
     * 建立驗證例外.
     *
     * @param message 錯誤訊息
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * 建立驗證例外（附帶原因）.
     *
     * @param message 錯誤訊息
     * @param cause   原始例外
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 建立欄位驗證例外.
     *
     * @param fieldName    欄位名稱
     * @param invalidValue 不合法的值
     * @param reason       原因說明
     */
    public ValidationException(String fieldName, Object invalidValue, String reason) {
        super("欄位「" + fieldName + "」值「" + invalidValue + "」驗證失敗：" + reason);
    }
}

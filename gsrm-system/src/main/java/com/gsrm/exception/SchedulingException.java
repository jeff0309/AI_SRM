package com.gsrm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 排程執行例外.
 *
 * <p>當排程引擎執行失敗，或 Session 狀態不允許目前操作時拋出此例外，
 * 對應 HTTP 422 Unprocessable Entity 狀態碼。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class SchedulingException extends RuntimeException {

    /**
     * 建立排程例外.
     *
     * @param message 錯誤訊息
     */
    public SchedulingException(String message) {
        super(message);
    }

    /**
     * 建立排程例外（附帶原因）.
     *
     * @param message 錯誤訊息
     * @param cause   原始例外
     */
    public SchedulingException(String message, Throwable cause) {
        super(message, cause);
    }
}

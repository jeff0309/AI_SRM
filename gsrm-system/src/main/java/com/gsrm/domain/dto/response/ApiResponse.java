package com.gsrm.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通用 API 回應 DTO.
 * 
 * <p>封裝所有 API 回應的標準格式。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 * @param <T> 回應資料類型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 是否成功.
     */
    private boolean success;

    /**
     * 訊息.
     */
    private String message;

    /**
     * 回應資料.
     */
    private T data;

    /**
     * 錯誤代碼.
     */
    private String errorCode;

    /**
     * 時間戳記.
     */
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 建立成功回應.
     * 
     * @param data 回應資料
     * @param <T> 資料類型
     * @return API 回應
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("操作成功")
                .data(data)
                .build();
    }

    /**
     * 建立成功回應（含訊息）.
     * 
     * @param message 訊息
     * @param data 回應資料
     * @param <T> 資料類型
     * @return API 回應
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * 建立成功回應（僅訊息）.
     * 
     * @param message 訊息
     * @return API 回應
     */
    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build();
    }

    /**
     * 建立失敗回應.
     * 
     * @param message 錯誤訊息
     * @param <T> 資料類型
     * @return API 回應
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }

    /**
     * 建立失敗回應（含錯誤代碼）.
     * 
     * @param message 錯誤訊息
     * @param errorCode 錯誤代碼
     * @param <T> 資料類型
     * @return API 回應
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
}

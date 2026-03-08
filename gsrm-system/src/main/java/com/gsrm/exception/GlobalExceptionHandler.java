package com.gsrm.exception;

import com.gsrm.domain.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

/**
 * 全域例外處理器.
 *
 * <p>攔截所有 Controller 層拋出的例外，統一轉換為標準 {@link ApiResponse} 格式回應給前端。</p>
 *
 * <ul>
 *   <li>{@link ResourceNotFoundException} → 404</li>
 *   <li>{@link ConflictException} → 409</li>
 *   <li>{@link ValidationException} → 400</li>
 *   <li>{@link SchedulingException} → 422</li>
 *   <li>{@link MethodArgumentNotValidException} → 400（Bean Validation）</li>
 *   <li>{@link AccessDeniedException} → 403</li>
 *   <li>{@link BadCredentialsException} → 401</li>
 *   <li>{@link Exception} → 500（所有未預期例外）</li>
 * </ul>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /* ─────────── 4xx 客戶端錯誤 ─────────── */

    /**
     * 處理資源不存在例外（404）.
     *
     * @param ex ResourceNotFoundException
     * @return 404 ApiResponse
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("[404] ResourceNotFoundException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 處理資源衝突例外（409）.
     *
     * @param ex ConflictException
     * @return 409 ApiResponse
     */
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(ConflictException ex) {
        log.warn("[409] ConflictException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 處理業務驗證例外（400）.
     *
     * @param ex ValidationException
     * @return 400 ApiResponse
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(ValidationException ex) {
        log.warn("[400] ValidationException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 處理 Bean Validation 例外（400），並整理欄位錯誤列表.
     *
     * @param ex MethodArgumentNotValidException
     * @return 400 ApiResponse（含欄位錯誤 Map）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            errors.put(fieldName, error.getDefaultMessage());
        });

        log.warn("[400] MethodArgumentNotValidException: {} field error(s)", errors.size());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.<Map<String, String>>builder()
                        .success(false)
                        .message("輸入資料驗證失敗")
                        .data(errors)
                        .build());
    }

    /**
     * 處理排程業務例外（422）.
     *
     * @param ex SchedulingException
     * @return 422 ApiResponse
     */
    @ExceptionHandler(SchedulingException.class)
    public ResponseEntity<ApiResponse<Void>> handleScheduling(SchedulingException ex) {
        log.error("[422] SchedulingException: {}", ex.getMessage(), ex.getCause());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(ex.getMessage()));
    }

    /**
     * 處理上傳檔案過大例外（400）.
     *
     * @param ex MaxUploadSizeExceededException
     * @return 400 ApiResponse
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("[400] MaxUploadSizeExceededException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("上傳檔案超過大小限制"));
    }

    /* ─────────── 認證 / 授權錯誤 ─────────── */

    /**
     * 處理登入憑證錯誤（401）.
     *
     * @param ex BadCredentialsException
     * @return 401 ApiResponse
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("[401] BadCredentialsException");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("使用者名稱或密碼錯誤"));
    }

    /**
     * 處理存取被拒例外（403）.
     *
     * @param ex AccessDeniedException
     * @return 403 ApiResponse
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        log.warn("[403] AccessDeniedException: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("權限不足，拒絕存取"));
    }

    /* ─────────── 5xx 伺服器錯誤 ─────────── */

    /**
     * 處理所有未預期的例外（500）.
     *
     * @param ex Exception
     * @return 500 ApiResponse
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("[500] Unexpected exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("系統發生未預期錯誤，請稍後再試"));
    }
}

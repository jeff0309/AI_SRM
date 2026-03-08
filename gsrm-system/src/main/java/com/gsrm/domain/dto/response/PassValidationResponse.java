package com.gsrm.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Pass 驗證結果回應 DTO.
 * 
 * @author Jeff
 * @since 2026-03-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassValidationResponse {

    /**
     * 是否存在衝突或違反限制.
     */
    private boolean isConflict;

    /**
     * 驗證訊息 (描述衝突原因).
     */
    private String message;

    /**
     * 衝突的 Pass ID (若有).
     */
    private Long conflictingPassId;

    /**
     * 衝突類型.
     */
    private String conflictType; // e.g., "STATION_MAINTENANCE", "PASS_OVERLAP"
}

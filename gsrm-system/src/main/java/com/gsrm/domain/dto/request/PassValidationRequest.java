package com.gsrm.domain.dto.request;

import com.gsrm.domain.enums.FrequencyBand;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Pass 驗證請求 DTO.
 * 
 * <p>用於手動排入前驗證衝突與限制條件。</p>
 * 
 * @author Jeff
 * @since 2026-03-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassValidationRequest {

    /**
     * Session ID.
     */
    @NotNull(message = "Session ID 不可為空")
    private Long sessionId;

    /**
     * 原始需求 ID (可選，若從現有需求拉入).
     */
    private Long requestId;

    /**
     * 地面站 ID.
     */
    @NotNull(message = "地面站 ID 不可為空")
    private Long groundStationId;

    /**
     * AOS 時間.
     */
    @NotNull(message = "AOS 時間不可為空")
    private LocalDateTime aos;

    /**
     * LOS 時間.
     */
    @NotNull(message = "LOS 時間不可為空")
    private LocalDateTime los;
}

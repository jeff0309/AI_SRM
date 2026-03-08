package com.gsrm.domain.dto.request;

import com.gsrm.domain.enums.FrequencyBand;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 手動新增 Pass 請求 DTO.
 * 
 * <p>用於在甘特圖上手動強制排入單一衛星需求。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManualPassRequest {

    /**
     * Session ID.
     */
    @NotNull(message = "Session ID 不可為空")
    private Long sessionId;

    /**
     * 衛星 ID.
     */
    @NotNull(message = "衛星 ID 不可為空")
    private Long satelliteId;

    /**
     * 地面站 ID.
     */
    @NotNull(message = "地面站 ID 不可為空")
    private Long groundStationId;

    /**
     * 頻段.
     */
    @NotNull(message = "頻段不可為空")
    private FrequencyBand frequencyBand;

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

    /**
     * 備註.
     */
    @Size(max = 500, message = "備註不可超過 500 字元")
    private String notes;
}

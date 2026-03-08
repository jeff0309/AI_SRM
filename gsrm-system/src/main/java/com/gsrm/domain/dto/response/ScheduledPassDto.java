package com.gsrm.domain.dto.response;

import com.gsrm.domain.enums.FrequencyBand;
import com.gsrm.domain.enums.PassStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 已排程 Pass 的資料傳輸物件.
 *
 * <p>避免直接序列化 ScheduledPass 實體（含 Lazy 關聯），
 * 將衛星、地面站等名稱平坦化後回傳給前端。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledPassDto {

    /** Pass ID. */
    private Long passId;

    /** 衛星 ID. */
    private Long satelliteId;

    /** 衛星名稱. */
    private String satelliteName;

    /** 地面站 ID. */
    private Long groundStationId;

    /** 地面站名稱. */
    private String groundStationName;

    /** 頻段. */
    private FrequencyBand frequencyBand;

    /** 原始 AOS. */
    private LocalDateTime originalAos;

    /** 原始 LOS. */
    private LocalDateTime originalLos;

    /** 排程後 AOS. */
    private LocalDateTime scheduledAos;

    /** 排程後 LOS. */
    private LocalDateTime scheduledLos;

    /** Pass 狀態. */
    private PassStatus status;

    /** 是否允許. */
    private Boolean isAllowed;

    /** 是否為手動強制. */
    private Boolean isForced;

    /** 縮短秒數. */
    private Integer shortenedSeconds;

    /** 排程時長（秒）. */
    private Long durationSeconds;

    /** 拒絕原因. */
    private String rejectionReason;

    /** 備註. */
    private String notes;
}

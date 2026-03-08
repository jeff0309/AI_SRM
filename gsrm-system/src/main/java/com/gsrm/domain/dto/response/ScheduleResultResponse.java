package com.gsrm.domain.dto.response;

import com.gsrm.domain.enums.ScheduleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 排程結果回應 DTO.
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResultResponse {

    /**
     * Session ID.
     */
    private Long sessionId;

    /**
     * Session 名稱.
     */
    private String sessionName;

    /**
     * 排程狀態.
     */
    private ScheduleStatus status;

    /**
     * 排程開始時間.
     */
    private LocalDateTime scheduleStartTime;

    /**
     * 排程結束時間.
     */
    private LocalDateTime scheduleEndTime;

    /**
     * 執行時間.
     */
    private LocalDateTime executedAt;

    /**
     * 總請求數.
     */
    private Integer totalRequests;

    /**
     * 成功排入數.
     */
    private Integer scheduledCount;

    /**
     * 被縮短數.
     */
    private Integer shortenedCount;

    /**
     * 被拒絕數.
     */
    private Integer rejectedCount;

    /**
     * 強制排入數.
     */
    private Integer forcedCount;

    /**
     * 成功率 (%).
     */
    private Double successRate;

    /**
     * 已解決的衝突數.
     */
    private Integer conflictsResolved;

    /**
     * 使用的策略名稱.
     */
    private String strategyUsed;

    /**
     * 執行耗時（毫秒）.
     */
    private Long executionTimeMs;
}

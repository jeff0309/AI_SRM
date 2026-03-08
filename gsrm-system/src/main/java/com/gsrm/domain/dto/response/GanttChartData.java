package com.gsrm.domain.dto.response;

import com.gsrm.domain.enums.FrequencyBand;
import com.gsrm.domain.enums.PassStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 甘特圖資料 DTO.
 * 
 * <p>用於前端甘特圖視覺化顯示的資料結構。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GanttChartData {

    /**
     * Session ID.
     */
    private Long sessionId;

    /**
     * Session 名稱.
     */
    private String sessionName;

    /**
     * 排程開始時間.
     */
    private LocalDateTime scheduleStartTime;

    /**
     * 排程結束時間.
     */
    private LocalDateTime scheduleEndTime;

    /**
     * 地面站資料列表.
     */
    private List<GroundStationRow> groundStations;

    /**
     * 地面站列（甘特圖的一列）.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroundStationRow {
        
        /**
         * 地面站 ID.
         */
        private Long groundStationId;
        
        /**
         * 地面站名稱.
         */
        private String groundStationName;
        
        /**
         * 頻段.
         */
        private FrequencyBand frequencyBand;
        
        /**
         * Pass 列表.
         */
        private List<PassItem> passes;
        
        /**
         * 維護時段列表.
         */
        private List<UnavailabilityItem> unavailabilities;
    }

    /**
     * Pass 項目.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassItem {
        
        /**
         * Pass ID.
         */
        private Long passId;
        
        /**
         * 衛星 ID.
         */
        private Long satelliteId;
        
        /**
         * 衛星名稱.
         */
        private String satelliteName;
        
        /**
         * 頻段.
         */
        private FrequencyBand frequencyBand;
        
        /**
         * 原始 AOS.
         */
        private LocalDateTime originalAos;
        
        /**
         * 原始 LOS.
         */
        private LocalDateTime originalLos;
        
        /**
         * 排程後 AOS.
         */
        private LocalDateTime scheduledAos;
        
        /**
         * 排程後 LOS.
         */
        private LocalDateTime scheduledLos;
        
        /**
         * 狀態.
         */
        private PassStatus status;
        
        /**
         * 是否被允許.
         */
        private Boolean isAllowed;
        
        /**
         * 是否強制排入.
         */
        private Boolean isForced;
        
        /**
         * 被縮短的秒數.
         */
        private Integer shortenedSeconds;
        
        /**
         * 持續時間（秒）.
         */
        private Long durationSeconds;
        
        /**
         * 備註.
         */
        private String notes;
    }

    /**
     * 維護時段項目.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnavailabilityItem {
        
        /**
         * 維護時段 ID.
         */
        private Long id;
        
        /**
         * 開始時間.
         */
        private LocalDateTime startTime;
        
        /**
         * 結束時間.
         */
        private LocalDateTime endTime;
        
        /**
         * 原因.
         */
        private String reason;
    }
}

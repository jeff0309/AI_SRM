package com.gsrm.scheduler.model;

import com.gsrm.domain.enums.FrequencyBand;
import com.gsrm.domain.enums.PassStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Pass 候選模型.
 * 
 * <p>排程引擎內部使用的 Pass 候選物件，包含排程計算所需的所有資訊。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PassCandidate {

    /**
     * 原始需求 ID.
     */
    private Long requestId;

    /**
     * 衛星 ID.
     */
    private Long satelliteId;

    /**
     * 衛星名稱.
     */
    private String satelliteName;

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
     * 原始 AOS.
     */
    private LocalDateTime originalAos;

    /**
     * 原始 LOS.
     */
    private LocalDateTime originalLos;

    /**
     * 計算後的 AOS.
     */
    private LocalDateTime scheduledAos;

    /**
     * 計算後的 LOS.
     */
    private LocalDateTime scheduledLos;

    /**
     * 衛星優先權權重.
     */
    private Integer priorityWeight;

    /**
     * 是否為緊急任務.
     */
    @Builder.Default
    private Boolean isEmergency = false;

    /**
     * 衛星的最小 Pass 持續時間（秒）.
     */
    private Integer minPassDuration;

    /**
     * 地面站的最小間隔時間（秒）.
     */
    private Integer stationGap;

    /**
     * 狀態.
     */
    @Builder.Default
    private PassStatus status = PassStatus.PENDING;

    /**
     * 是否被允許排入.
     */
    @Builder.Default
    private Boolean isAllowed = false;

    /**
     * 被縮短的秒數.
     */
    @Builder.Default
    private Integer shortenedSeconds = 0;

    /**
     * 拒絕原因.
     */
    private String rejectionReason;

    /**
     * 衝突的 Pass ID.
     */
    private Long conflictWithId;

    /**
     * 取得原始時間槽.
     * 
     * @return 原始時間槽
     */
    public TimeSlot getOriginalTimeSlot() {
        return TimeSlot.builder()
                .startTime(originalAos)
                .endTime(originalLos)
                .build();
    }

    /**
     * 取得計算後的時間槽.
     * 
     * @return 計算後的時間槽
     */
    public TimeSlot getScheduledTimeSlot() {
        LocalDateTime aos = scheduledAos != null ? scheduledAos : originalAos;
        LocalDateTime los = scheduledLos != null ? scheduledLos : originalLos;
        return TimeSlot.builder()
                .startTime(aos)
                .endTime(los)
                .build();
    }

    /**
     * 計算原始持續時間（秒）.
     * 
     * @return 原始持續時間
     */
    public long getOriginalDurationSeconds() {
        return getOriginalTimeSlot().getDurationSeconds();
    }

    /**
     * 計算計算後的持續時間（秒）.
     * 
     * @return 計算後的持續時間
     */
    public long getScheduledDurationSeconds() {
        return getScheduledTimeSlot().getDurationSeconds();
    }

    /**
     * 檢查是否與另一個候選衝突.
     * 
     * @param other 另一個候選
     * @return 如果衝突則回傳 true
     */
    public boolean conflictsWith(PassCandidate other) {
        if (other == null || !groundStationId.equals(other.groundStationId)) {
            return false;
        }
        return getScheduledTimeSlot().overlapsWithGap(
                other.getScheduledTimeSlot(), 
                stationGap != null ? stationGap : 0);
    }

    /**
     * 標記為已排入.
     */
    public void markAsScheduled() {
        this.status = PassStatus.SCHEDULED;
        this.isAllowed = true;
        this.scheduledAos = this.originalAos;
        this.scheduledLos = this.originalLos;
    }

    /**
     * 標記為已縮短.
     * 
     * @param newAos 新的 AOS
     * @param newLos 新的 LOS
     */
    public void markAsShortened(LocalDateTime newAos, LocalDateTime newLos) {
        this.status = PassStatus.SHORTENED;
        this.isAllowed = true;
        this.scheduledAos = newAos;
        this.scheduledLos = newLos;
        this.shortenedSeconds = (int)(getOriginalDurationSeconds() - 
                java.time.Duration.between(newAos, newLos).getSeconds());
    }

    /**
     * 標記為已拒絕.
     * 
     * @param reason 拒絕原因
     * @param conflictId 衝突的 Pass ID
     */
    public void markAsRejected(String reason, Long conflictId) {
        this.status = PassStatus.REJECTED;
        this.isAllowed = false;
        this.rejectionReason = reason;
        this.conflictWithId = conflictId;
    }

    /**
     * 比較優先權.
     * 
     * @param other 另一個候選
     * @return 如果此候選優先權較高則回傳正數
     */
    public int comparePriority(PassCandidate other) {
        if (other == null) {
            return 1;
        }
        // 緊急任務優先
        if (Boolean.TRUE.equals(this.isEmergency) && !Boolean.TRUE.equals(other.isEmergency)) {
            return 1;
        }
        if (!Boolean.TRUE.equals(this.isEmergency) && Boolean.TRUE.equals(other.isEmergency)) {
            return -1;
        }
        // 比較優先權權重
        return Integer.compare(
                this.priorityWeight != null ? this.priorityWeight : 0, 
                other.priorityWeight != null ? other.priorityWeight : 0);
    }
}

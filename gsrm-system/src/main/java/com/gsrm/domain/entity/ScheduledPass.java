package com.gsrm.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gsrm.domain.enums.FrequencyBand;
import com.gsrm.domain.enums.PassStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 已排程 Pass 實體類別.
 * 
 * <p>代表經過排程引擎處理後的 Pass 結果，包含實際排程的時間、狀態等資訊。
 * 此實體與原始需求 (SatelliteRequest) 關聯。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Entity
@Table(name = "scheduled_passes", indexes = {
    @Index(name = "idx_pass_session", columnList = "schedule_session_id"),
    @Index(name = "idx_pass_satellite", columnList = "satellite_id"),
    @Index(name = "idx_pass_station", columnList = "ground_station_id"),
    @Index(name = "idx_pass_aos", columnList = "scheduled_aos"),
    @Index(name = "idx_pass_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledPass {

    /**
     * 已排程 Pass 唯一識別碼.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所屬排程 Session.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_session_id", nullable = false)
    private ScheduleSession scheduleSession;

    /**
     * 原始需求（如果來自匯入的需求）.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "satellite_request_id")
    private SatelliteRequest satelliteRequest;

    /**
     * 關聯衛星.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "satellite_id", nullable = false)
    private Satellite satellite;

    /**
     * 目標地面站.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ground_station_id", nullable = false)
    private GroundStation groundStation;

    /**
     * 頻段.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_band", nullable = false, length = 10)
    private FrequencyBand frequencyBand;

    /**
     * 原始 AOS 時間.
     */
    @Column(name = "original_aos", nullable = false)
    private LocalDateTime originalAos;

    /**
     * 原始 LOS 時間.
     */
    @Column(name = "original_los", nullable = false)
    private LocalDateTime originalLos;

    /**
     * 實際排程的 AOS 時間.
     */
    @Column(name = "scheduled_aos")
    private LocalDateTime scheduledAos;

    /**
     * 實際排程的 LOS 時間.
     */
    @Column(name = "scheduled_los")
    private LocalDateTime scheduledLos;

    /**
     * Pass 狀態.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PassStatus status = PassStatus.PENDING;

    /**
     * 是否被允許排入.
     */
    @Column(name = "is_allowed", nullable = false)
    @Builder.Default
    private Boolean isAllowed = false;

    /**
     * 被縮短的秒數.
     */
    @Column(name = "shortened_seconds")
    @Builder.Default
    private Integer shortenedSeconds = 0;

    /**
     * 拒絕原因（如果被拒絕）.
     */
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    /**
     * 衝突的 Pass ID（如果因衝突被拒絕或縮短）.
     */
    @Column(name = "conflict_with_pass_id")
    private Long conflictWithPassId;

    /**
     * 是否為手動強制排入.
     */
    @Column(name = "is_forced", nullable = false)
    @Builder.Default
    private Boolean isForced = false;

    /**
     * 備註.
     */
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * 建立時間.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 建立者 ID.
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 計算原始 Pass 持續時間（秒）.
     * 
     * @return 原始持續時間秒數
     */
    public long getOriginalDurationSeconds() {
        if (originalAos == null || originalLos == null) {
            return 0;
        }
        return Duration.between(originalAos, originalLos).getSeconds();
    }

    /**
     * 計算實際排程的 Pass 持續時間（秒）.
     * 
     * @return 實際持續時間秒數
     */
    public long getScheduledDurationSeconds() {
        if (scheduledAos == null || scheduledLos == null) {
            return 0;
        }
        return Duration.between(scheduledAos, scheduledLos).getSeconds();
    }

    /**
     * 檢查是否成功排入.
     * 
     * @return 如果成功排入則回傳 true
     */
    public boolean isSuccessful() {
        return status != null && status.isSuccessful();
    }

    /**
     * 檢查是否被縮短.
     * 
     * @return 如果被縮短則回傳 true
     */
    public boolean wasShortened() {
        return shortenedSeconds != null && shortenedSeconds > 0;
    }

    /**
     * 檢查與另一個 Pass 是否在同一地面站上時間重疊.
     * 
     * @param other 另一個 Pass
     * @return 如果在同一地面站且時間重疊則回傳 true
     */
    public boolean conflictsWith(ScheduledPass other) {
        if (other == null || groundStation == null || other.groundStation == null) {
            return false;
        }
        
        // 檢查是否在同一地面站
        if (!groundStation.getId().equals(other.groundStation.getId())) {
            return false;
        }
        
        LocalDateTime thisStart = scheduledAos != null ? scheduledAos : originalAos;
        LocalDateTime thisEnd = scheduledLos != null ? scheduledLos : originalLos;
        LocalDateTime otherStart = other.scheduledAos != null ? other.scheduledAos : other.originalAos;
        LocalDateTime otherEnd = other.scheduledLos != null ? other.scheduledLos : other.originalLos;
        
        // 考慮地面站的前置與回復時間
        int gap = groundStation.getMinimumGap();
        
        return thisStart.isBefore(otherEnd.plusSeconds(gap)) && 
               thisEnd.plusSeconds(gap).isAfter(otherStart);
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
     * @param newAos 縮短後的 AOS
     * @param newLos 縮短後的 LOS
     */
    public void markAsShortened(LocalDateTime newAos, LocalDateTime newLos) {
        this.status = PassStatus.SHORTENED;
        this.isAllowed = true;
        this.scheduledAos = newAos;
        this.scheduledLos = newLos;
        this.shortenedSeconds = (int)(getOriginalDurationSeconds() - 
            Duration.between(newAos, newLos).getSeconds());
    }

    /**
     * 標記為已拒絕.
     * 
     * @param reason 拒絕原因
     * @param conflictPassId 衝突的 Pass ID
     */
    public void markAsRejected(String reason, Long conflictPassId) {
        this.status = PassStatus.REJECTED;
        this.isAllowed = false;
        this.rejectionReason = reason;
        this.conflictWithPassId = conflictPassId;
    }

    /**
     * 標記為強制排入.
     * 
     * @param userId 執行強制排入的使用者 ID
     */
    public void markAsForced(Long userId) {
        this.status = PassStatus.FORCED;
        this.isAllowed = true;
        this.isForced = true;
        this.scheduledAos = this.originalAos;
        this.scheduledLos = this.originalLos;
        this.createdBy = userId;
    }
}

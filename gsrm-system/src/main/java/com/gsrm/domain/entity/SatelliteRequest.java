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
 * 衛星需求實體類別.
 * 
 * <p>代表衛星 Pass 的排程需求，包含 AOS/LOS 時間、頻段等資訊。
 * 這是從外部系統匯入或手動建立的原始需求。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Entity
@Table(name = "satellite_requests", indexes = {
    @Index(name = "idx_request_session", columnList = "schedule_session_id"),
    @Index(name = "idx_request_satellite", columnList = "satellite_id"),
    @Index(name = "idx_request_station", columnList = "ground_station_id"),
    @Index(name = "idx_request_aos", columnList = "aos"),
    @Index(name = "idx_request_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SatelliteRequest {

    /**
     * 需求唯一識別碼.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 外部請求 ID（來源系統的識別碼）.
     */
    @Column(name = "external_request_id", length = 100)
    private String externalRequestId;

    /**
     * 所屬排程 Session.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_session_id", nullable = false)
    private ScheduleSession scheduleSession;

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
     * 信號獲取時間 (Acquisition of Signal).
     */
    @Column(name = "aos", nullable = false)
    private LocalDateTime aos;

    /**
     * 信號丟失時間 (Loss of Signal).
     */
    @Column(name = "los", nullable = false)
    private LocalDateTime los;

    /**
     * 最大仰角（度）.
     */
    @Column(name = "max_elevation")
    private Double maxElevation;

    /**
     * 請求優先級.
     * 1-10，1 為最高優先。
     */
    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 5;

    /**
     * 是否為緊急任務.
     */
    @Column(name = "is_emergency", nullable = false)
    @Builder.Default
    private Boolean isEmergency = false;

    /**
     * 處理狀態.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private PassStatus status = PassStatus.PENDING;

    /**
     * 備註.
     */
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * 匯入批次 ID.
     */
    @Column(name = "import_batch_id", length = 50)
    private String importBatchId;

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
     * @return 持續時間秒數
     */
    public long getOriginalDurationSeconds() {
        if (aos == null || los == null) {
            return 0;
        }
        return Duration.between(aos, los).getSeconds();
    }

    /**
     * 檢查需求是否與另一個需求在時間上重疊.
     * 
     * @param other 另一個需求
     * @return 如果有重疊則回傳 true
     */
    public boolean overlaps(SatelliteRequest other) {
        if (other == null || aos == null || los == null || 
            other.aos == null || other.los == null) {
            return false;
        }
        return aos.isBefore(other.los) && los.isAfter(other.aos);
    }

    /**
     * 檢查需求是否在指定地面站.
     * 
     * @param stationId 地面站 ID
     * @return 如果在指定地面站則回傳 true
     */
    public boolean isAtStation(Long stationId) {
        return groundStation != null && groundStation.getId().equals(stationId);
    }

    /**
     * 檢查是否為待處理狀態.
     * 
     * @return 如果是待處理則回傳 true
     */
    public boolean isPending() {
        return status == PassStatus.PENDING;
    }

    /**
     * 標記為已排入.
     */
    public void markAsScheduled() {
        this.status = PassStatus.SCHEDULED;
    }

    /**
     * 標記為已拒絕.
     * 
     * @param reason 拒絕原因
     */
    public void markAsRejected(String reason) {
        this.status = PassStatus.REJECTED;
        this.notes = reason;
    }
}

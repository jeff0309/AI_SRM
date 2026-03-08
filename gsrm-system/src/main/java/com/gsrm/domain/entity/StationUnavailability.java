package com.gsrm.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 地面站維護時段實體類別.
 * 
 * <p>代表地面站的不可用時段，例如定期維護、設備檢修等。
 * 在此時段內，地面站無法接收衛星信號。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Entity
@Table(name = "station_unavailabilities", indexes = {
    @Index(name = "idx_unavail_station", columnList = "ground_station_id"),
    @Index(name = "idx_unavail_time", columnList = "start_time, end_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationUnavailability {

    /**
     * 維護時段唯一識別碼.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所屬地面站.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ground_station_id", nullable = false)
    private GroundStation groundStation;

    /**
     * 維護開始時間.
     */
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    /**
     * 維護結束時間.
     */
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * 維護原因.
     */
    @Column(name = "reason", length = 500)
    private String reason;

    /**
     * 維護類型（定期維護、緊急維修等）.
     */
    @Column(name = "maintenance_type", length = 50)
    private String maintenanceType;

    /**
     * 是否為週期性維護.
     */
    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

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
     * 檢查指定時間是否在維護時段內.
     * 
     * @param time 要檢查的時間
     * @return 如果在維護時段內則回傳 true
     */
    public boolean containsTime(LocalDateTime time) {
        if (time == null || startTime == null || endTime == null) {
            return false;
        }
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    /**
     * 檢查指定時間區間是否與維護時段重疊.
     * 
     * @param checkStart 區間開始時間
     * @param checkEnd 區間結束時間
     * @return 如果有重疊則回傳 true
     */
    public boolean overlaps(LocalDateTime checkStart, LocalDateTime checkEnd) {
        if (startTime == null || endTime == null || checkStart == null || checkEnd == null) {
            return false;
        }
        return checkStart.isBefore(endTime) && checkEnd.isAfter(startTime);
    }

    /**
     * 取得維護時段持續時間（秒）.
     * 
     * @return 持續時間秒數
     */
    public long getDurationSeconds() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, endTime).getSeconds();
    }
}

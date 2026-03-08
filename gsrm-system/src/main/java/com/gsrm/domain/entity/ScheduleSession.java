package com.gsrm.domain.entity;

import com.gsrm.domain.enums.ScheduleStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 排程 Session 實體類別.
 * 
 * <p>代表一個排程任務的 Session，定義排程的時間範圍、關聯的衛星群與地面站群。
 * 排程演算法將基於此 Session 的內容進行計算。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Entity
@Table(name = "schedule_sessions", indexes = {
    @Index(name = "idx_session_status", columnList = "status"),
    @Index(name = "idx_session_time", columnList = "schedule_start_time, schedule_end_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleSession {

    /**
     * Session 唯一識別碼.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Session 名稱.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * Session 描述.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 排程開始時間.
     */
    @Column(name = "schedule_start_time", nullable = false)
    private LocalDateTime scheduleStartTime;

    /**
     * 排程結束時間.
     */
    @Column(name = "schedule_end_time", nullable = false)
    private LocalDateTime scheduleEndTime;

    /**
     * 排程狀態.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.DRAFT;

    /**
     * 關聯的衛星群（Eager 載入以便序列化 ID）.
     */
    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "session_satellites",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "satellite_id")
    )
    @Builder.Default
    private Set<Satellite> satellites = new HashSet<>();

    /**
     * 關聯的地面站群（Eager 載入以便序列化 ID）.
     */
    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "session_ground_stations",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "ground_station_id")
    )
    @Builder.Default
    private Set<GroundStation> groundStations = new HashSet<>();

    /**
     * 關聯的衛星需求.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "scheduleSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<SatelliteRequest> requests = new HashSet<>();

    /**
     * 關聯的已排程 Pass.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "scheduleSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ScheduledPass> scheduledPasses = new HashSet<>();

    /**
     * 使用的縮短策略名稱.
     */
    @Column(name = "shortening_strategy", length = 50)
    @Builder.Default
    private String shorteningStrategy = "PROPORTIONAL";

    /**
     * 排程執行時間.
     */
    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    /**
     * 執行者 ID.
     */
    @Column(name = "executed_by")
    private Long executedBy;

    /**
     * 總請求數.
     */
    @Column(name = "total_requests")
    @Builder.Default
    private Integer totalRequests = 0;

    /**
     * 成功排入數.
     */
    @Column(name = "scheduled_count")
    @Builder.Default
    private Integer scheduledCount = 0;

    /**
     * 被拒絕數.
     */
    @Column(name = "rejected_count")
    @Builder.Default
    private Integer rejectedCount = 0;

    /**
     * 建立時間.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最後更新時間.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 建立者 ID.
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 檢查是否可以執行排程.
     * 
     * @return 如果狀態允許執行排程則回傳 true
     */
    public boolean canExecute() {
        return status != null && status.canSchedule();
    }

    /**
     * 檢查是否可以重置.
     * 
     * @return 如果可以重置則回傳 true
     */
    public boolean canReset() {
        return status != null && status.canReset();
    }

    /**
     * 計算成功率.
     * 
     * @return 成功率百分比 (0-100)
     */
    public double getSuccessRate() {
        if (totalRequests == null || totalRequests == 0) {
            return 0.0;
        }
        return (scheduledCount != null ? scheduledCount : 0) * 100.0 / totalRequests;
    }

    /**
     * 計算排程時間範圍的持續時間（小時）.
     * 
     * @return 持續時間小時數
     */
    public long getDurationHours() {
        if (scheduleStartTime == null || scheduleEndTime == null) {
            return 0;
        }
        return java.time.Duration.between(scheduleStartTime, scheduleEndTime).toHours();
    }

    /**
     * 新增衛星到 Session.
     * 
     * @param satellite 要新增的衛星
     */
    public void addSatellite(Satellite satellite) {
        if (satellites == null) {
            satellites = new HashSet<>();
        }
        satellites.add(satellite);
    }

    /**
     * 新增地面站到 Session.
     * 
     * @param groundStation 要新增的地面站
     */
    public void addGroundStation(GroundStation groundStation) {
        if (groundStations == null) {
            groundStations = new HashSet<>();
        }
        groundStations.add(groundStation);
    }

    /**
     * 取得關聯衛星的 ID 集合（用於 JSON 序列化）.
     *
     * @return 衛星 ID 集合
     */
    @JsonProperty("satelliteIds")
    public Set<Long> getSatelliteIdSet() {
        if (satellites == null) return new HashSet<>();
        return satellites.stream().map(Satellite::getId).collect(Collectors.toSet());
    }

    /**
     * 取得關聯地面站的 ID 集合（用於 JSON 序列化）.
     *
     * @return 地面站 ID 集合
     */
    @JsonProperty("groundStationIds")
    public Set<Long> getGroundStationIdSet() {
        if (groundStations == null) return new HashSet<>();
        return groundStations.stream().map(GroundStation::getId).collect(Collectors.toSet());
    }

    /**
     * 標記為處理中.
     */
    public void markAsProcessing() {
        this.status = ScheduleStatus.PROCESSING;
    }

    /**
     * 標記為已完成.
     * 
     * @param executorId 執行者 ID
     */
    public void markAsCompleted(Long executorId) {
        this.status = ScheduleStatus.COMPLETED;
        this.executedAt = LocalDateTime.now();
        this.executedBy = executorId;
    }

    /**
     * 重置排程結果.
     */
    public void reset() {
        this.status = ScheduleStatus.DRAFT;
        this.executedAt = null;
        this.executedBy = null;
        this.scheduledCount = 0;
        this.rejectedCount = 0;
        if (scheduledPasses != null) {
            scheduledPasses.clear();
        }
        if (requests != null) {
            requests.forEach(r -> r.setStatus(com.gsrm.domain.enums.PassStatus.PENDING));
        }
    }
}

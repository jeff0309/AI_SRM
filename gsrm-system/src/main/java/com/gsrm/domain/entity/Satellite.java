package com.gsrm.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gsrm.domain.enums.FrequencyBand;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 衛星實體類別.
 * 
 * <p>代表衛星的基本資訊，包含所屬公司、最低 Pass 需求、優先權等屬性。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Entity
@Table(name = "satellites", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Satellite {

    /**
     * 衛星唯一識別碼.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 衛星名稱.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 衛星代碼（NORAD ID 或內部代碼）.
     */
    @Column(name = "code", length = 20)
    private String code;

    /**
     * 所屬公司/組織.
     */
    @Column(name = "company", length = 100)
    private String company;

    /**
     * 衛星使用的頻段.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_band", nullable = false, length = 10)
    private FrequencyBand frequencyBand;

    /**
     * 每日最低分配 Pass 數.
     * 排程系統會嘗試為此衛星每日至少分配這麼多個 Pass。
     */
    @Column(name = "min_daily_passes", nullable = false)
    @Builder.Default
    private Integer minDailyPasses = 1;

    /**
     * 最小有效 Pass 秒數.
     * 低於此秒數的 Pass 將被視為無效，不會被排入。
     */
    @Column(name = "min_pass_duration", nullable = false)
    @Builder.Default
    private Integer minPassDuration = 60;

    /**
     * 衛星衝突時的絕對優先權權重.
     * 數值越高，在衝突時優先保留此衛星的 Pass。
     * 範圍：1-100
     */
    @Column(name = "priority_weight", nullable = false)
    @Builder.Default
    private Integer priorityWeight = 50;

    /**
     * 是否為緊急任務衛星.
     * 緊急任務衛星在排程時具有最高優先權。
     */
    @Column(name = "is_emergency", nullable = false)
    @Builder.Default
    private Boolean isEmergency = false;

    /**
     * 是否啟用.
     */
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 描述.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 聯絡人.
     */
    @Column(name = "contact_person", length = 100)
    private String contactPerson;

    /**
     * 聯絡電子郵件.
     */
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    /**
     * 地面站偏好優先序列表.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "satellite", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("preferenceOrder ASC")
    @Builder.Default
    private List<GroundStationPreference> groundStationPreferences = new ArrayList<>();

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
     * 檢查 Pass 時長是否滿足最小要求.
     * 
     * @param durationSeconds Pass 持續時間（秒）
     * @return 如果滿足最小要求則回傳 true
     */
    public boolean isPassDurationValid(int durationSeconds) {
        return durationSeconds >= minPassDuration;
    }

    /**
     * 取得衛星的完整描述.
     * 
     * @return 格式化的描述字串
     */
    public String getFullDescription() {
        return String.format("%s (%s) - %s Band, Priority: %d", 
            name, 
            code != null ? code : "N/A",
            frequencyBand,
            priorityWeight);
    }

    /**
     * 比較兩顆衛星的優先權.
     * 
     * @param other 另一顆衛星
     * @return 如果此衛星優先權較高則回傳正數
     */
    public int comparePriority(Satellite other) {
        if (other == null) {
            return 1;
        }
        // 緊急衛星優先
        if (Boolean.TRUE.equals(this.isEmergency) && !Boolean.TRUE.equals(other.isEmergency)) {
            return 1;
        }
        if (!Boolean.TRUE.equals(this.isEmergency) && Boolean.TRUE.equals(other.isEmergency)) {
            return -1;
        }
        // 比較優先權權重
        return Integer.compare(this.priorityWeight, other.priorityWeight);
    }
}

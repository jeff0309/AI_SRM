package com.gsrm.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gsrm.domain.enums.FrequencyBand;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 地面站實體類別.
 * 
 * <p>代表衛星地面站的基本資訊，包含位置、支援頻段、前置與回復時間等屬性。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Entity
@Table(name = "ground_stations", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroundStation {

    /**
     * 地面站唯一識別碼.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 地面站名稱.
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 地面站代碼（簡稱）.
     */
    @Column(name = "code", length = 20)
    private String code;

    /**
     * 經度（度）.
     * 範圍：-180 到 180
     */
    @Column(name = "longitude", nullable = false)
    private Double longitude;

    /**
     * 緯度（度）.
     * 範圍：-90 到 90
     */
    @Column(name = "latitude", nullable = false)
    private Double latitude;

    /**
     * 高度（公尺）.
     */
    @Column(name = "altitude")
    private Double altitude;

    /**
     * 前置準備時間（秒）.
     * 在開始接收信號前所需的準備時間。
     */
    @Column(name = "setup_time", nullable = false)
    @Builder.Default
    private Integer setupTime = 300;

    /**
     * 回復時間（秒）.
     * 結束接收後到可進行下一次接收所需的時間。
     */
    @Column(name = "teardown_time", nullable = false)
    @Builder.Default
    private Integer teardownTime = 300;

    /**
     * 支援的頻段.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_band", nullable = false, length = 10)
    private FrequencyBand frequencyBand;

    /**
     * 最小仰角（度）.
     * 低於此仰角的 Pass 將無法接收。
     */
    @Column(name = "min_elevation")
    @Builder.Default
    private Double minElevation = 5.0;

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
     * 聯絡電話.
     */
    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    /**
     * 維護時段列表.
     */
    @JsonIgnore
    @OneToMany(mappedBy = "groundStation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<StationUnavailability> unavailabilities = new HashSet<>();

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
     * 檢查地面站是否支援指定頻段.
     * 
     * @param requiredBand 需要的頻段
     * @return 如果支援則回傳 true
     */
    public boolean supportsFrequencyBand(FrequencyBand requiredBand) {
        if (frequencyBand == null || requiredBand == null) {
            return false;
        }
        return frequencyBand.isCompatibleWith(requiredBand);
    }

    /**
     * 計算兩個 Pass 之間所需的最小間隔（秒）.
     * 
     * @return 前一個 Pass 結束到下一個 Pass 開始所需的時間
     */
    public int getMinimumGap() {
        return teardownTime + setupTime;
    }

    /**
     * 取得地面站完整的位置描述.
     * 
     * @return 格式化的位置字串
     */
    public String getLocationDescription() {
        return String.format("%.4f°, %.4f° (Alt: %.1fm)", 
            longitude, latitude, altitude != null ? altitude : 0.0);
    }
}

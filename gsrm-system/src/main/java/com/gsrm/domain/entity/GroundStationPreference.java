package com.gsrm.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

/**
 * 地面站偏好實體類別.
 * 
 * <p>定義衛星對地面站的偏好優先順序。當同一時間有多個地面站可用時，
 * 系統會依據此優先順序選擇地面站。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Entity
@Table(name = "ground_station_preferences", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"satellite_id", "ground_station_id"}),
    @UniqueConstraint(columnNames = {"satellite_id", "preference_order"})
}, indexes = {
    @Index(name = "idx_pref_satellite", columnList = "satellite_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroundStationPreference {

    /**
     * 偏好設定唯一識別碼.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 所屬衛星.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "satellite_id", nullable = false)
    private Satellite satellite;

    /**
     * 偏好的地面站.
     */
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ground_station_id", nullable = false)
    private GroundStation groundStation;

    /**
     * 偏好順序.
     * 數值越小優先權越高（1 為最高優先）。
     */
    @Column(name = "preference_order", nullable = false)
    private Integer preferenceOrder;

    /**
     * 是否為必要地面站.
     * 如果為 true，則此衛星的 Pass 只能排入此地面站。
     */
    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = false;

    /**
     * 備註.
     */
    @Column(name = "notes", length = 255)
    private String notes;

    /**
     * 比較兩個偏好設定的順序.
     * 
     * @param other 另一個偏好設定
     * @return 如果此偏好順序較前則回傳負數
     */
    public int compareTo(GroundStationPreference other) {
        if (other == null) {
            return -1;
        }
        return Integer.compare(this.preferenceOrder, other.preferenceOrder);
    }
}

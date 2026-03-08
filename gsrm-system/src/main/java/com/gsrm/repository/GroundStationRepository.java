package com.gsrm.repository;

import com.gsrm.domain.entity.GroundStation;
import com.gsrm.domain.enums.FrequencyBand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 地面站資料存取介面.
 * 
 * <p>提供地面站實體的 CRUD 操作與自訂查詢方法。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Repository
public interface GroundStationRepository extends JpaRepository<GroundStation, Long> {

    /**
     * 依名稱查詢.
     * 
     * @param name 地面站名稱
     * @return 地面站 Optional
     */
    Optional<GroundStation> findByName(String name);

    /**
     * 依代碼查詢.
     * 
     * @param code 地面站代碼
     * @return 地面站 Optional
     */
    Optional<GroundStation> findByCode(String code);

    /**
     * 檢查名稱是否存在.
     * 
     * @param name 地面站名稱
     * @return 如果存在則回傳 true
     */
    boolean existsByName(String name);

    /**
     * 依頻段查詢地面站.
     * 
     * @param frequencyBand 頻段
     * @return 地面站列表
     */
    List<GroundStation> findByFrequencyBand(FrequencyBand frequencyBand);

    /**
     * 查詢已啟用的地面站.
     * 
     * @return 已啟用的地面站列表
     */
    List<GroundStation> findByEnabledTrue();

    /**
     * 查詢已啟用且支援指定頻段的地面站.
     * 
     * @param frequencyBand 頻段
     * @return 地面站列表
     */
    @Query("SELECT g FROM GroundStation g WHERE g.enabled = true AND " +
           "(g.frequencyBand = :band OR g.frequencyBand = 'XS')")
    List<GroundStation> findEnabledByFrequencyBand(@Param("band") FrequencyBand frequencyBand);

    /**
     * 依經緯度範圍查詢地面站.
     * 
     * @param minLon 最小經度
     * @param maxLon 最大經度
     * @param minLat 最小緯度
     * @param maxLat 最大緯度
     * @return 地面站列表
     */
    @Query("SELECT g FROM GroundStation g WHERE g.enabled = true AND " +
           "g.longitude BETWEEN :minLon AND :maxLon AND " +
           "g.latitude BETWEEN :minLat AND :maxLat")
    List<GroundStation> findByLocationRange(
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat);

    /**
     * 依多個 ID 查詢地面站.
     * 
     * @param ids 地面站 ID 列表
     * @return 地面站列表
     */
    List<GroundStation> findByIdIn(List<Long> ids);

    /**
     * 計算啟用的地面站數量.
     * 
     * @return 啟用的地面站數量
     */
    long countByEnabledTrue();

    /**
     * 依頻段計算地面站數量.
     * 
     * @param frequencyBand 頻段
     * @return 地面站數量
     */
    long countByFrequencyBand(FrequencyBand frequencyBand);
}

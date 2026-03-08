package com.gsrm.repository;

import com.gsrm.domain.entity.Satellite;
import com.gsrm.domain.enums.FrequencyBand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 衛星資料存取介面.
 * 
 * <p>提供衛星實體的 CRUD 操作與自訂查詢方法。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Repository
public interface SatelliteRepository extends JpaRepository<Satellite, Long> {

    /**
     * 依名稱查詢.
     * 
     * @param name 衛星名稱
     * @return 衛星 Optional
     */
    Optional<Satellite> findByName(String name);

    /**
     * 依代碼查詢.
     * 
     * @param code 衛星代碼
     * @return 衛星 Optional
     */
    Optional<Satellite> findByCode(String code);

    /**
     * 檢查名稱是否存在.
     * 
     * @param name 衛星名稱
     * @return 如果存在則回傳 true
     */
    boolean existsByName(String name);

    /**
     * 依公司查詢衛星.
     * 
     * @param company 公司名稱
     * @return 衛星列表
     */
    List<Satellite> findByCompany(String company);

    /**
     * 依頻段查詢衛星.
     * 
     * @param frequencyBand 頻段
     * @return 衛星列表
     */
    List<Satellite> findByFrequencyBand(FrequencyBand frequencyBand);

    /**
     * 查詢已啟用的衛星.
     * 
     * @return 已啟用的衛星列表
     */
    List<Satellite> findByEnabledTrue();

    /**
     * 查詢緊急任務衛星.
     * 
     * @return 緊急任務衛星列表
     */
    List<Satellite> findByIsEmergencyTrue();

    /**
     * 依優先權權重排序查詢已啟用的衛星.
     * 
     * @return 衛星列表（依優先權降序）
     */
    @Query("SELECT s FROM Satellite s WHERE s.enabled = true ORDER BY s.priorityWeight DESC")
    List<Satellite> findEnabledOrderByPriority();

    /**
     * 依多個 ID 查詢衛星.
     * 
     * @param ids 衛星 ID 列表
     * @return 衛星列表
     */
    List<Satellite> findByIdIn(List<Long> ids);

    /**
     * 查詢有每日最低 Pass 需求的衛星.
     * 
     * @param minPasses 最低 Pass 數
     * @return 衛星列表
     */
    @Query("SELECT s FROM Satellite s WHERE s.enabled = true AND s.minDailyPasses >= :minPasses")
    List<Satellite> findWithMinDailyPassesGreaterThan(@Param("minPasses") Integer minPasses);

    /**
     * 取得所有不重複的公司名稱.
     * 
     * @return 公司名稱列表
     */
    @Query("SELECT DISTINCT s.company FROM Satellite s WHERE s.company IS NOT NULL ORDER BY s.company")
    List<String> findDistinctCompanies();

    /**
     * 計算啟用的衛星數量.
     * 
     * @return 啟用的衛星數量
     */
    long countByEnabledTrue();
}

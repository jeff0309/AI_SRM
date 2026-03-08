package com.gsrm.repository;

import com.gsrm.domain.entity.SatelliteRequest;
import com.gsrm.domain.enums.PassStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 衛星需求資料存取介面.
 * 
 * <p>提供衛星需求實體的 CRUD 操作與自訂查詢方法。
 * 繼承 JpaSpecificationExecutor 以支援動態查詢。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Repository
public interface SatelliteRequestRepository extends JpaRepository<SatelliteRequest, Long>,
                                                     JpaSpecificationExecutor<SatelliteRequest> {

    /**
     * 依排程 Session ID 查詢需求.
     * 
     * @param sessionId Session ID
     * @return 需求列表
     */
    List<SatelliteRequest> findByScheduleSessionId(Long sessionId);

    /**
     * 依排程 Session ID 分頁查詢需求.
     * 
     * @param sessionId Session ID
     * @param pageable 分頁資訊
     * @return 需求分頁結果
     */
    Page<SatelliteRequest> findByScheduleSessionId(Long sessionId, Pageable pageable);

    /**
     * 依衛星 ID 查詢需求.
     * 
     * @param satelliteId 衛星 ID
     * @return 需求列表
     */
    List<SatelliteRequest> findBySatelliteId(Long satelliteId);

    /**
     * 依地面站 ID 查詢需求.
     * 
     * @param groundStationId 地面站 ID
     * @return 需求列表
     */
    List<SatelliteRequest> findByGroundStationId(Long groundStationId);

    /**
     * 依狀態查詢需求.
     * 
     * @param status 狀態
     * @return 需求列表
     */
    List<SatelliteRequest> findByStatus(PassStatus status);

    /**
     * 依 Session ID 和狀態查詢需求.
     * 
     * @param sessionId Session ID
     * @param status 狀態
     * @return 需求列表
     */
    List<SatelliteRequest> findByScheduleSessionIdAndStatus(Long sessionId, PassStatus status);

    /**
     * 依 Session ID 查詢待處理的需求（依 AOS 排序）.
     * 
     * @param sessionId Session ID
     * @return 需求列表
     */
    @Query("SELECT r FROM SatelliteRequest r WHERE r.scheduleSession.id = :sessionId " +
           "AND r.status = 'PENDING' ORDER BY r.aos")
    List<SatelliteRequest> findPendingBySessionOrderByAos(@Param("sessionId") Long sessionId);

    /**
     * 依時間區間查詢需求.
     * 
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 需求列表
     */
    @Query("SELECT r FROM SatelliteRequest r WHERE r.aos >= :startTime AND r.los <= :endTime")
    List<SatelliteRequest> findByTimeRange(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 依地面站和時間區間查詢需求.
     * 
     * @param groundStationId 地面站 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 需求列表
     */
    @Query("SELECT r FROM SatelliteRequest r WHERE r.groundStation.id = :stationId " +
           "AND r.aos >= :startTime AND r.los <= :endTime ORDER BY r.aos")
    List<SatelliteRequest> findByGroundStationAndTimeRange(
            @Param("stationId") Long groundStationId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查詢緊急任務需求.
     * 
     * @param sessionId Session ID
     * @return 緊急需求列表
     */
    List<SatelliteRequest> findByScheduleSessionIdAndIsEmergencyTrue(Long sessionId);

    /**
     * 依外部請求 ID 查詢.
     * 
     * @param externalRequestId 外部請求 ID
     * @return 需求列表
     */
    List<SatelliteRequest> findByExternalRequestId(String externalRequestId);

    /**
     * 依匯入批次 ID 查詢.
     * 
     * @param importBatchId 匯入批次 ID
     * @return 需求列表
     */
    List<SatelliteRequest> findByImportBatchId(String importBatchId);

    /**
     * 計算 Session 中各狀態的需求數量.
     * 
     * @param sessionId Session ID
     * @param status 狀態
     * @return 需求數量
     */
    long countByScheduleSessionIdAndStatus(Long sessionId, PassStatus status);

    /**
     * 刪除 Session 的所有需求.
     * 
     * @param sessionId Session ID
     */
    void deleteByScheduleSessionId(Long sessionId);
}

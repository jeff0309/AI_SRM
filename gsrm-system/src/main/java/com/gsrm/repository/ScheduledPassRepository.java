package com.gsrm.repository;

import com.gsrm.domain.entity.ScheduledPass;
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
 * 已排程 Pass 資料存取介面.
 * 
 * <p>提供已排程 Pass 實體的 CRUD 操作與自訂查詢方法。
 * 繼承 JpaSpecificationExecutor 以支援動態查詢（歷史資料查詢）。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Repository
public interface ScheduledPassRepository extends JpaRepository<ScheduledPass, Long>,
                                                  JpaSpecificationExecutor<ScheduledPass> {

    /**
     * 依排程 Session ID 查詢 Pass.
     * 
     * @param sessionId Session ID
     * @return Pass 列表
     */
    List<ScheduledPass> findByScheduleSessionId(Long sessionId);

    /**
     * 依排程 Session ID 分頁查詢 Pass.
     * 
     * @param sessionId Session ID
     * @param pageable 分頁資訊
     * @return Pass 分頁結果
     */
    Page<ScheduledPass> findByScheduleSessionId(Long sessionId, Pageable pageable);

    /**
     * 依衛星 ID 查詢 Pass.
     * 
     * @param satelliteId 衛星 ID
     * @return Pass 列表
     */
    List<ScheduledPass> findBySatelliteId(Long satelliteId);

    /**
     * 依地面站 ID 查詢 Pass.
     * 
     * @param groundStationId 地面站 ID
     * @return Pass 列表
     */
    List<ScheduledPass> findByGroundStationId(Long groundStationId);

    /**
     * 依狀態查詢 Pass.
     * 
     * @param status 狀態
     * @return Pass 列表
     */
    List<ScheduledPass> findByStatus(PassStatus status);

    /**
     * 依 Session ID 和狀態查詢 Pass.
     * 
     * @param sessionId Session ID
     * @param status 狀態
     * @return Pass 列表
     */
    List<ScheduledPass> findByScheduleSessionIdAndStatus(Long sessionId, PassStatus status);

    /**
     * 依 Session ID 查詢成功排入的 Pass（含衛星與地面站關聯）.
     *
     * @param sessionId Session ID
     * @return Pass 列表
     */
    @Query("SELECT p FROM ScheduledPass p " +
           "JOIN FETCH p.satellite " +
           "JOIN FETCH p.groundStation " +
           "WHERE p.scheduleSession.id = :sessionId " +
           "AND p.isAllowed = true ORDER BY p.scheduledAos")
    List<ScheduledPass> findAllowedBySessionOrderByAos(@Param("sessionId") Long sessionId);

    /**
     * 依地面站和時間區間查詢已排入的 Pass.
     * 
     * @param groundStationId 地面站 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return Pass 列表
     */
    @Query("SELECT p FROM ScheduledPass p WHERE p.groundStation.id = :stationId " +
           "AND p.isAllowed = true " +
           "AND p.scheduledAos >= :startTime AND p.scheduledLos <= :endTime " +
           "ORDER BY p.scheduledAos")
    List<ScheduledPass> findAllowedByGroundStationAndTimeRange(
            @Param("stationId") Long groundStationId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查詢強制排入的 Pass.
     * 
     * @param sessionId Session ID
     * @return Pass 列表
     */
    List<ScheduledPass> findByScheduleSessionIdAndIsForcedTrue(Long sessionId);

    /**
     * 查詢被縮短的 Pass.
     * 
     * @param sessionId Session ID
     * @return Pass 列表
     */
    @Query("SELECT p FROM ScheduledPass p WHERE p.scheduleSession.id = :sessionId " +
           "AND p.shortenedSeconds > 0")
    List<ScheduledPass> findShortenedBySession(@Param("sessionId") Long sessionId);

    /**
     * 計算 Session 中各狀態的 Pass 數量.
     * 
     * @param sessionId Session ID
     * @param status 狀態
     * @return Pass 數量
     */
    long countByScheduleSessionIdAndStatus(Long sessionId, PassStatus status);

    /**
     * 計算 Session 中成功排入的 Pass 數量.
     * 
     * @param sessionId Session ID
     * @return Pass 數量
     */
    long countByScheduleSessionIdAndIsAllowedTrue(Long sessionId);

    /**
     * 依衛星和時間區間查詢 Pass（用於歷史查詢）.
     * 
     * @param satelliteId 衛星 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @param pageable 分頁資訊
     * @return Pass 分頁結果
     */
    @Query("SELECT p FROM ScheduledPass p WHERE p.satellite.id = :satelliteId " +
           "AND p.originalAos >= :startTime AND p.originalLos <= :endTime")
    Page<ScheduledPass> findBySatelliteAndTimeRange(
            @Param("satelliteId") Long satelliteId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);

    /**
     * 取得 Session 的甘特圖資料.
     * 
     * @param sessionId Session ID
     * @return Pass 列表
     */
    @Query("SELECT p FROM ScheduledPass p " +
           "JOIN FETCH p.satellite " +
           "JOIN FETCH p.groundStation " +
           "WHERE p.scheduleSession.id = :sessionId " +
           "ORDER BY p.groundStation.name, p.scheduledAos")
    List<ScheduledPass> findGanttDataBySession(@Param("sessionId") Long sessionId);

    /**
     * 刪除 Session 的所有 Pass.
     * 
     * @param sessionId Session ID
     */
    void deleteByScheduleSessionId(Long sessionId);
}

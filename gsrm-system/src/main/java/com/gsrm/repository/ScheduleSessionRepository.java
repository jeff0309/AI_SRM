package com.gsrm.repository;

import com.gsrm.domain.entity.ScheduleSession;
import com.gsrm.domain.enums.ScheduleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 排程 Session 資料存取介面.
 * 
 * <p>提供排程 Session 實體的 CRUD 操作與自訂查詢方法。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Repository
public interface ScheduleSessionRepository extends JpaRepository<ScheduleSession, Long> {

    /**
     * 依名稱查詢.
     * 
     * @param name Session 名稱
     * @return Session Optional
     */
    Optional<ScheduleSession> findByName(String name);

    /**
     * 依狀態查詢.
     * 
     * @param status 排程狀態
     * @return Session 列表
     */
    List<ScheduleSession> findByStatus(ScheduleStatus status);

    /**
     * 依狀態分頁查詢.
     * 
     * @param status 排程狀態
     * @param pageable 分頁資訊
     * @return Session 分頁結果
     */
    Page<ScheduleSession> findByStatus(ScheduleStatus status, Pageable pageable);

    /**
     * 依時間區間查詢 Session.
     * 
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return Session 列表
     */
    @Query("SELECT s FROM ScheduleSession s WHERE s.scheduleStartTime >= :startTime " +
           "AND s.scheduleEndTime <= :endTime ORDER BY s.scheduleStartTime")
    List<ScheduleSession> findByScheduleTimeRange(@Param("startTime") LocalDateTime startTime,
                                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 查詢與指定時間區間重疊的 Session.
     * 
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return Session 列表
     */
    @Query("SELECT s FROM ScheduleSession s WHERE s.scheduleStartTime < :endTime " +
           "AND s.scheduleEndTime > :startTime")
    List<ScheduleSession> findOverlapping(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 依建立者查詢.
     * 
     * @param createdBy 建立者 ID
     * @return Session 列表
     */
    List<ScheduleSession> findByCreatedBy(Long createdBy);

    /**
     * 查詢已完成的 Session（依執行時間降序）.
     * 
     * @param pageable 分頁資訊
     * @return Session 分頁結果
     */
    @Query("SELECT s FROM ScheduleSession s WHERE s.status = 'COMPLETED' ORDER BY s.executedAt DESC")
    Page<ScheduleSession> findCompletedOrderByExecutedAt(Pageable pageable);

    /**
     * 查詢最近建立的 Session.
     * 
     * @param limit 限制數量
     * @return Session 列表
     */
    @Query("SELECT s FROM ScheduleSession s ORDER BY s.createdAt DESC")
    List<ScheduleSession> findRecentSessions(Pageable pageable);

    /**
     * 依狀態計算 Session 數量.
     * 
     * @param status 排程狀態
     * @return Session 數量
     */
    long countByStatus(ScheduleStatus status);

    /**
     * 查詢包含指定衛星的 Session.
     * 
     * @param satelliteId 衛星 ID
     * @return Session 列表
     */
    @Query("SELECT s FROM ScheduleSession s JOIN s.satellites sat WHERE sat.id = :satelliteId")
    List<ScheduleSession> findBySatelliteId(@Param("satelliteId") Long satelliteId);

    /**
     * 查詢包含指定地面站的 Session.
     * 
     * @param groundStationId 地面站 ID
     * @return Session 列表
     */
    @Query("SELECT s FROM ScheduleSession s JOIN s.groundStations gs WHERE gs.id = :groundStationId")
    List<ScheduleSession> findByGroundStationId(@Param("groundStationId") Long groundStationId);
}

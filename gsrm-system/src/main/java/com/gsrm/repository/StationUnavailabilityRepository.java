package com.gsrm.repository;

import com.gsrm.domain.entity.StationUnavailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 地面站維護時段資料存取介面.
 * 
 * <p>提供地面站維護時段實體的 CRUD 操作與自訂查詢方法。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Repository
public interface StationUnavailabilityRepository extends JpaRepository<StationUnavailability, Long> {

    /**
     * 依地面站 ID 查詢維護時段.
     * 
     * @param groundStationId 地面站 ID
     * @return 維護時段列表
     */
    List<StationUnavailability> findByGroundStationId(Long groundStationId);

    /**
     * 依地面站 ID 和時間區間查詢維護時段.
     * 
     * @param groundStationId 地面站 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 維護時段列表
     */
    @Query("SELECT u FROM StationUnavailability u WHERE u.groundStation.id = :stationId " +
           "AND u.startTime < :endTime AND u.endTime > :startTime")
    List<StationUnavailability> findByGroundStationAndTimeRange(
            @Param("stationId") Long groundStationId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 檢查地面站在指定時間是否可用.
     * 
     * @param groundStationId 地面站 ID
     * @param checkTime 要檢查的時間
     * @return 如果有維護時段包含此時間則回傳 true（不可用）
     */
    @Query("SELECT COUNT(u) > 0 FROM StationUnavailability u " +
           "WHERE u.groundStation.id = :stationId " +
           "AND :checkTime >= u.startTime AND :checkTime <= u.endTime")
    boolean isUnavailableAt(@Param("stationId") Long groundStationId,
                            @Param("checkTime") LocalDateTime checkTime);

    /**
     * 查詢與指定時間區間重疊的維護時段.
     * 
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 維護時段列表
     */
    @Query("SELECT u FROM StationUnavailability u WHERE " +
           "u.startTime < :endTime AND u.endTime > :startTime " +
           "ORDER BY u.groundStation.name, u.startTime")
    List<StationUnavailability> findOverlapping(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 查詢未來的維護時段.
     * 
     * @param fromTime 起始時間
     * @return 維護時段列表
     */
    @Query("SELECT u FROM StationUnavailability u WHERE u.startTime >= :fromTime " +
           "ORDER BY u.startTime")
    List<StationUnavailability> findUpcoming(@Param("fromTime") LocalDateTime fromTime);

    /**
     * 依維護類型查詢.
     * 
     * @param maintenanceType 維護類型
     * @return 維護時段列表
     */
    List<StationUnavailability> findByMaintenanceType(String maintenanceType);

    /**
     * 查詢週期性維護時段.
     * 
     * @return 維護時段列表
     */
    List<StationUnavailability> findByIsRecurringTrue();

    /**
     * 刪除地面站的所有維護時段.
     * 
     * @param groundStationId 地面站 ID
     */
    void deleteByGroundStationId(Long groundStationId);

    /**
     * 刪除指定時間區間內的維護時段.
     * 
     * @param groundStationId 地面站 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     */
    @Query("DELETE FROM StationUnavailability u WHERE u.groundStation.id = :stationId " +
           "AND u.startTime >= :startTime AND u.endTime <= :endTime")
    void deleteByGroundStationAndTimeRange(@Param("stationId") Long groundStationId,
                                            @Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);
}

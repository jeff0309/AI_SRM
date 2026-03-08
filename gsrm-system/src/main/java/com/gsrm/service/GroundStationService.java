package com.gsrm.service;

import com.gsrm.domain.dto.request.GroundStationRequest;
import com.gsrm.domain.entity.GroundStation;
import com.gsrm.domain.entity.StationUnavailability;
import com.gsrm.domain.enums.FrequencyBand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 地面站服務介面.
 * 
 * <p>定義地面站 CRUD 操作與維護時段管理。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
public interface GroundStationService {

    /**
     * 建立新的地面站.
     * 
     * @param request 地面站請求
     * @return 建立的地面站
     */
    GroundStation createGroundStation(GroundStationRequest request);

    /**
     * 更新地面站.
     * 
     * @param id 地面站 ID
     * @param request 更新請求
     * @return 更新後的地面站
     */
    GroundStation updateGroundStation(Long id, GroundStationRequest request);

    /**
     * 依 ID 取得地面站.
     * 
     * @param id 地面站 ID
     * @return 地面站實體
     */
    GroundStation getGroundStationById(Long id);

    /**
     * 依名稱取得地面站.
     * 
     * @param name 地面站名稱
     * @return 地面站實體
     */
    GroundStation getGroundStationByName(String name);

    /**
     * 取得所有地面站（分頁）.
     * 
     * @param pageable 分頁資訊
     * @return 地面站分頁結果
     */
    Page<GroundStation> getAllGroundStations(Pageable pageable);

    /**
     * 取得所有已啟用的地面站.
     * 
     * @return 地面站列表
     */
    List<GroundStation> getEnabledGroundStations();

    /**
     * 依頻段取得地面站.
     * 
     * @param frequencyBand 頻段
     * @return 地面站列表
     */
    List<GroundStation> getGroundStationsByFrequencyBand(FrequencyBand frequencyBand);

    /**
     * 刪除地面站.
     * 
     * @param id 地面站 ID
     */
    void deleteGroundStation(Long id);

    /**
     * 啟用/停用地面站.
     * 
     * @param id 地面站 ID
     * @param enabled 是否啟用
     * @return 更新後的地面站
     */
    GroundStation setGroundStationEnabled(Long id, boolean enabled);

    /**
     * 新增維護時段.
     * 
     * @param groundStationId 地面站 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @param reason 維護原因
     * @param userId 建立者 ID
     * @return 維護時段實體
     */
    StationUnavailability addUnavailability(Long groundStationId, LocalDateTime startTime,
                                             LocalDateTime endTime, String reason, Long userId);

    /**
     * 批次匯入維護時段.
     * 
     * @param unavailabilities 維護時段列表
     * @param userId 建立者 ID
     * @return 匯入的數量
     */
    int importUnavailabilities(List<StationUnavailability> unavailabilities, Long userId);

    /**
     * 取得地面站的維護時段.
     * 
     * @param groundStationId 地面站 ID
     * @param startTime 查詢開始時間
     * @param endTime 查詢結束時間
     * @return 維護時段列表
     */
    List<StationUnavailability> getUnavailabilities(Long groundStationId, 
                                                     LocalDateTime startTime, 
                                                     LocalDateTime endTime);

    /**
     * 刪除維護時段.
     * 
     * @param unavailabilityId 維護時段 ID
     */
    void deleteUnavailability(Long unavailabilityId);

    /**
     * 檢查地面站在指定時間是否可用.
     * 
     * @param groundStationId 地面站 ID
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 如果可用則回傳 true
     */
    boolean isAvailable(Long groundStationId, LocalDateTime startTime, LocalDateTime endTime);
}

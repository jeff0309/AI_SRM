package com.gsrm.service;

import com.gsrm.domain.dto.request.SatelliteCreateRequest;
import com.gsrm.domain.entity.Satellite;
import com.gsrm.domain.enums.FrequencyBand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 衛星服務介面.
 *
 * <p>定義衛星 CRUD 操作與地面站偏好管理的業務邏輯。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
public interface SatelliteService {

    /**
     * 建立新衛星.
     *
     * @param request 建立請求
     * @return 建立完成的衛星
     */
    Satellite createSatellite(SatelliteCreateRequest request);

    /**
     * 更新衛星資料.
     *
     * @param id      衛星 ID
     * @param request 更新請求
     * @return 更新後的衛星
     */
    Satellite updateSatellite(Long id, SatelliteCreateRequest request);

    /**
     * 依 ID 取得衛星.
     *
     * @param id 衛星 ID
     * @return 衛星實體
     */
    Satellite getSatelliteById(Long id);

    /**
     * 依名稱取得衛星.
     *
     * @param name 衛星名稱
     * @return 衛星實體
     */
    Satellite getSatelliteByName(String name);

    /**
     * 取得所有衛星（分頁）.
     *
     * @param pageable 分頁資訊
     * @return 衛星分頁結果
     */
    Page<Satellite> getAllSatellites(Pageable pageable);

    /**
     * 取得所有已啟用的衛星.
     *
     * @return 衛星列表
     */
    List<Satellite> getEnabledSatellites();

    /**
     * 依頻段取得衛星.
     *
     * @param frequencyBand 頻段
     * @return 衛星列表
     */
    List<Satellite> getSatellitesByFrequencyBand(FrequencyBand frequencyBand);

    /**
     * 取得緊急任務衛星.
     *
     * @return 緊急任務衛星列表
     */
    List<Satellite> getEmergencySatellites();

    /**
     * 依優先權排序取得已啟用衛星.
     *
     * @return 衛星列表（優先權由高到低）
     */
    List<Satellite> getSatellitesByPriority();

    /**
     * 取得所有公司名稱.
     *
     * @return 公司名稱列表
     */
    List<String> getDistinctCompanies();

    /**
     * 刪除衛星.
     *
     * @param id 衛星 ID
     */
    void deleteSatellite(Long id);

    /**
     * 啟用 / 停用衛星.
     *
     * @param id      衛星 ID
     * @param enabled {@code true} 表示啟用
     * @return 更新後的衛星
     */
    Satellite setSatelliteEnabled(Long id, boolean enabled);

    /**
     * 設定衛星為緊急任務衛星.
     *
     * @param id          衛星 ID
     * @param isEmergency {@code true} 表示緊急任務
     * @return 更新後的衛星
     */
    Satellite setEmergency(Long id, boolean isEmergency);
}

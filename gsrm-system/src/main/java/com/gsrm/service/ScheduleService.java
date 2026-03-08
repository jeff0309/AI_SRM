package com.gsrm.service;

import com.gsrm.domain.dto.request.ManualPassRequest;
import com.gsrm.domain.dto.request.ScheduleSessionRequest;
import com.gsrm.domain.dto.response.GanttChartData;
import com.gsrm.domain.dto.response.ScheduleResultResponse;
import com.gsrm.domain.dto.response.ScheduledPassDto;
import com.gsrm.domain.entity.ScheduleSession;
import com.gsrm.domain.entity.ScheduledPass;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 排程服務介面.
 * 
 * <p>定義排程 Session 管理、排程執行、甘特圖資料取得等核心操作。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
public interface ScheduleService {

    /**
     * 建立新的排程 Session.
     * 
     * @param request 排程 Session 請求
     * @param userId 建立者 ID
     * @return 建立的 Session
     */
    ScheduleSession createSession(ScheduleSessionRequest request, Long userId);

    /**
     * 更新排程 Session.
     * 
     * @param sessionId Session ID
     * @param request 更新請求
     * @return 更新後的 Session
     */
    ScheduleSession updateSession(Long sessionId, ScheduleSessionRequest request);

    /**
     * 依 ID 取得 Session.
     * 
     * @param sessionId Session ID
     * @return Session 實體
     */
    ScheduleSession getSessionById(Long sessionId);

    /**
     * 取得所有 Session（分頁）.
     * 
     * @param pageable 分頁資訊
     * @return Session 分頁結果
     */
    Page<ScheduleSession> getAllSessions(Pageable pageable);

    /**
     * 刪除 Session.
     * 
     * @param sessionId Session ID
     */
    void deleteSession(Long sessionId);

    /**
     * 執行排程演算.
     * 
     * @param sessionId Session ID
     * @param userId 執行者 ID
     * @param strategyName 使用的策略名稱（可選）
     * @return 排程結果
     */
    ScheduleResultResponse executeScheduling(Long sessionId, Long userId, String strategyName);

    /**
     * 重置 Session 排程結果.
     * 
     * @param sessionId Session ID
     */
    void resetSession(Long sessionId);

    /**
     * 取得 Session 的甘特圖資料.
     * 
     * @param sessionId Session ID
     * @return 甘特圖資料
     */
    GanttChartData getGanttChartData(Long sessionId);

    /**
     * 手動新增 Pass 到 Session.
     * 
     * @param request 手動 Pass 請求
     * @param userId 執行者 ID
     * @return 新增的 Pass
     */
    ScheduledPass addManualPass(ManualPassRequest request, Long userId);

    /**
     * 移除已排程的 Pass.
     * 
     * @param passId Pass ID
     */
    void removePass(Long passId);

    /**
     * 取得 Session 的所有已排程 Pass（含衛星/地面站名稱）.
     *
     * @param sessionId Session ID
     * @return Pass DTO 列表
     */
    List<ScheduledPassDto> getScheduledPasses(Long sessionId);

    /**
     * 取得可用的排程策略列表.
     * 
     * @return 策略名稱列表
     */
    List<String> getAvailableStrategies();
}

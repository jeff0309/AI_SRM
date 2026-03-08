package com.gsrm.controller;

import com.gsrm.domain.dto.response.ApiResponse;
import com.gsrm.domain.dto.response.ScheduledPassDto;
import com.gsrm.domain.entity.ScheduledPass;
import com.gsrm.domain.entity.ScheduleSession;
import com.gsrm.domain.entity.SatelliteRequest;
import com.gsrm.domain.enums.PassStatus;
import com.gsrm.domain.enums.ScheduleStatus;
import com.gsrm.repository.ScheduledPassRepository;
import com.gsrm.repository.SatelliteRequestRepository;
import com.gsrm.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 歷史資料查詢控制器.
 *
 * <p>提供排程 Session 歷史、Pass 歷史、需求歷史的多維度查詢 API。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
@Tag(name = "History", description = "歷史資料查詢 API")
public class HistoryController {

    private final ScheduleService            scheduleService;
    private final ScheduledPassRepository    passRepository;
    private final SatelliteRequestRepository requestRepository;

    /**
     * 取得所有 Session 歷史（分頁）.
     *
     * @param pageable 分頁參數
     * @return Session 分頁結果
     */
    @GetMapping("/sessions")
    @Operation(summary = "Session 歷史列表", description = "分頁取得所有排程 Session 歷史")
    public ResponseEntity<ApiResponse<Page<ScheduleSession>>> getSessionHistory(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getAllSessions(pageable)));
    }

    /**
     * 取得指定 Session 的所有 Pass 紀錄.
     *
     * @param sessionId Session ID
     * @return Pass DTO 列表
     */
    @GetMapping("/sessions/{sessionId}/passes")
    @Operation(summary = "Session Pass 列表", description = "取得指定 Session 的所有已排程 Pass")
    public ResponseEntity<ApiResponse<List<ScheduledPassDto>>> getPassesBySession(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getScheduledPasses(sessionId)));
    }

    /**
     * 取得指定 Session 的需求列表（分頁）.
     *
     * @param sessionId Session ID
     * @param pageable  分頁參數
     * @return 需求分頁結果
     */
    @GetMapping("/sessions/{sessionId}/requests")
    @Operation(summary = "Session 需求列表", description = "分頁取得指定 Session 的衛星需求")
    public ResponseEntity<ApiResponse<Page<SatelliteRequest>>> getRequestsBySession(
            @PathVariable Long sessionId,
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.success(requestRepository.findByScheduleSessionId(sessionId, pageable)));
    }

    /**
     * 依狀態查詢需求.
     *
     * @param sessionId Session ID
     * @param status    Pass 狀態
     * @return 需求列表
     */
    @GetMapping("/sessions/{sessionId}/requests/by-status/{status}")
    @Operation(summary = "依狀態查詢需求", description = "取得指定 Session 中特定狀態的需求")
    public ResponseEntity<ApiResponse<List<SatelliteRequest>>> getRequestsByStatus(
            @PathVariable Long sessionId,
            @PathVariable PassStatus status) {
        return ResponseEntity.ok(ApiResponse.success(
                requestRepository.findByScheduleSessionIdAndStatus(sessionId, status)));
    }

    /**
     * 依衛星查詢 Pass 歷史.
     *
     * @param satelliteId 衛星 ID
     * @return Pass 列表
     */
    @GetMapping("/satellites/{satelliteId}/passes")
    @Operation(summary = "衛星 Pass 歷史", description = "取得指定衛星的所有 Pass 歷史")
    public ResponseEntity<ApiResponse<List<SatelliteRequest>>> getPassHistoryBySatellite(
            @PathVariable Long satelliteId) {
        return ResponseEntity.ok(
                ApiResponse.success(requestRepository.findBySatelliteId(satelliteId)));
    }

    /**
     * 依地面站查詢 Pass 歷史.
     *
     * @param groundStationId 地面站 ID
     * @return Pass 列表
     */
    @GetMapping("/ground-stations/{groundStationId}/passes")
    @Operation(summary = "地面站 Pass 歷史", description = "取得指定地面站的所有 Pass 歷史")
    public ResponseEntity<ApiResponse<List<SatelliteRequest>>> getPassHistoryByGroundStation(
            @PathVariable Long groundStationId) {
        return ResponseEntity.ok(
                ApiResponse.success(requestRepository.findByGroundStationId(groundStationId)));
    }

    /**
     * 依時間區間查詢需求.
     *
     * @param startTime 開始時間（ISO 8601）
     * @param endTime   結束時間（ISO 8601）
     * @return 需求列表
     */
    @GetMapping("/requests/by-time")
    @Operation(summary = "依時間查詢需求", description = "依時間區間查詢衛星需求")
    public ResponseEntity<ApiResponse<List<SatelliteRequest>>> getRequestsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return ResponseEntity.ok(ApiResponse.success(
                requestRepository.findByTimeRange(startTime, endTime)));
    }

    /**
     * 查詢緊急任務的需求.
     *
     * @param sessionId Session ID
     * @return 緊急任務需求列表
     */
    @GetMapping("/sessions/{sessionId}/requests/emergency")
    @Operation(summary = "緊急任務需求", description = "取得指定 Session 中的緊急任務需求")
    public ResponseEntity<ApiResponse<List<SatelliteRequest>>> getEmergencyRequests(
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResponse.success(
                requestRepository.findByScheduleSessionIdAndIsEmergencyTrue(sessionId)));
    }
}

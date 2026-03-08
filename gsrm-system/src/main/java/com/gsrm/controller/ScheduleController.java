package com.gsrm.controller;

import com.gsrm.domain.dto.request.ManualPassRequest;
import com.gsrm.domain.dto.request.PassValidationRequest;
import com.gsrm.domain.dto.request.ScheduleSessionRequest;
import com.gsrm.domain.dto.response.ApiResponse;
import com.gsrm.domain.dto.response.GanttChartData;
import com.gsrm.domain.dto.response.PassValidationResponse;
import com.gsrm.domain.dto.response.ScheduleResultResponse;
import com.gsrm.domain.dto.response.ScheduledPassDto;
import com.gsrm.domain.entity.ScheduleSession;
import com.gsrm.domain.entity.ScheduledPass;
import com.gsrm.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排程控制器.
 * 
 * <p>提供排程 Session 管理、排程執行、甘特圖資料取得等 REST API。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@RestController
@RequestMapping("/schedule")
@Tag(name = "Schedule", description = "排程管理 API")
public class ScheduleController {

    private final ScheduleService scheduleService;

    /**
     * 建構排程控制器.
     * 
     * @param scheduleService 排程服務
     */
    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * 取得所有 Session（分頁）.
     * 
     * @param pageable 分頁參數
     * @return Session 分頁結果
     */
    @GetMapping("/sessions")
    @Operation(summary = "取得所有排程 Session", description = "分頁取得所有排程 Session")
    public ResponseEntity<ApiResponse<Page<ScheduleSession>>> getAllSessions(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<ScheduleSession> sessions = scheduleService.getAllSessions(pageable);
        return ResponseEntity.ok(ApiResponse.success(sessions));
    }

    /**
     * 依 ID 取得 Session.
     * 
     * @param id Session ID
     * @return Session 資料
     */
    @GetMapping("/sessions/{id}")
    @Operation(summary = "取得排程 Session", description = "依 ID 取得單一排程 Session")
    public ResponseEntity<ApiResponse<ScheduleSession>> getSessionById(@PathVariable Long id) {
        ScheduleSession session = scheduleService.getSessionById(id);
        return ResponseEntity.ok(ApiResponse.success(session));
    }

    /**
     * 建立新的 Session.
     * 
     * @param request Session 請求
     * @param userDetails 當前使用者
     * @return 建立的 Session
     */
    @PostMapping("/sessions")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "建立排程 Session", description = "建立新的排程 Session")
    public ResponseEntity<ApiResponse<ScheduleSession>> createSession(
            @Valid @RequestBody ScheduleSessionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 從 userDetails 取得使用者 ID（此處簡化處理）
        Long userId = 1L; // 實際應從認證資訊中取得
        ScheduleSession session = scheduleService.createSession(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("排程 Session 建立成功", session));
    }

    /**
     * 更新 Session.
     * 
     * @param id Session ID
     * @param request 更新請求
     * @return 更新後的 Session
     */
    @PutMapping("/sessions/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "更新排程 Session", description = "更新現有排程 Session")
    public ResponseEntity<ApiResponse<ScheduleSession>> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody ScheduleSessionRequest request) {
        ScheduleSession session = scheduleService.updateSession(id, request);
        return ResponseEntity.ok(ApiResponse.success("排程 Session 更新成功", session));
    }

    /**
     * 刪除 Session.
     * 
     * @param id Session ID
     * @return 刪除結果
     */
    @DeleteMapping("/sessions/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "刪除排程 Session", description = "刪除指定的排程 Session")
    public ResponseEntity<ApiResponse<Void>> deleteSession(@PathVariable Long id) {
        scheduleService.deleteSession(id);
        return ResponseEntity.ok(ApiResponse.success("排程 Session 刪除成功"));
    }

    /**
     * 執行排程演算.
     * 
     * @param id Session ID
     * @param strategy 使用的策略名稱（可選）
     * @param userDetails 當前使用者
     * @return 排程結果
     */
    @PostMapping("/sessions/{id}/execute")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "執行排程", description = "對指定 Session 執行排程演算")
    public ResponseEntity<ApiResponse<ScheduleResultResponse>> executeScheduling(
            @PathVariable Long id,
            @RequestParam(required = false) String strategy,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = 1L; // 實際應從認證資訊中取得
        ScheduleResultResponse result = scheduleService.executeScheduling(id, userId, strategy);
        return ResponseEntity.ok(ApiResponse.success("排程執行完成", result));
    }

    /**
     * 重置 Session 排程結果.
     * 
     * @param id Session ID
     * @return 重置結果
     */
    @PostMapping("/sessions/{id}/reset")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "重置排程", description = "清空指定 Session 的排程結果")
    public ResponseEntity<ApiResponse<Void>> resetSession(@PathVariable Long id) {
        scheduleService.resetSession(id);
        return ResponseEntity.ok(ApiResponse.success("排程已重置"));
    }

    /**
     * 取得甘特圖資料.
     * 
     * @param id Session ID
     * @return 甘特圖資料
     */
    @GetMapping("/sessions/{id}/gantt")
    @Operation(summary = "取得甘特圖資料", description = "取得指定 Session 的甘特圖視覺化資料")
    public ResponseEntity<ApiResponse<GanttChartData>> getGanttChartData(@PathVariable Long id) {
        GanttChartData ganttData = scheduleService.getGanttChartData(id);
        return ResponseEntity.ok(ApiResponse.success(ganttData));
    }

    /**
     * 取得 Session 的所有已排程 Pass.
     *
     * @param id Session ID
     * @return Pass DTO 列表
     */
    @GetMapping("/sessions/{id}/passes")
    @Operation(summary = "取得已排程 Pass", description = "取得指定 Session 的所有已排程 Pass")
    public ResponseEntity<ApiResponse<List<ScheduledPassDto>>> getScheduledPasses(@PathVariable Long id) {
        List<ScheduledPassDto> passes = scheduleService.getScheduledPasses(id);
        return ResponseEntity.ok(ApiResponse.success(passes));
    }

    /**
     * 手動新增 Pass.
     * 
     * @param request 手動 Pass 請求
     * @param userDetails 當前使用者
     * @return 新增的 Pass
     */
    @PostMapping("/passes/manual")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "手動新增 Pass", description = "手動強制排入單一 Pass")
    public ResponseEntity<ApiResponse<ScheduledPass>> addManualPass(
            @Valid @RequestBody ManualPassRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = 1L;
        ScheduledPass pass = scheduleService.addManualPass(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pass 已手動新增", pass));
    }

    /**
     * 移除已排程的 Pass.
     * 
     * @param passId Pass ID
     * @return 移除結果
     */
    @DeleteMapping("/passes/{passId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "移除 Pass", description = "從排程中移除指定的 Pass")
    public ResponseEntity<ApiResponse<Void>> removePass(@PathVariable Long passId) {
        scheduleService.removePass(passId);
        return ResponseEntity.ok(ApiResponse.success("Pass 已移除"));
    }

    /**
     * 驗證單一 Pass 是否可排入.
     * 
     * @param request 驗證請求
     * @return 驗證結果
     */
    @PostMapping("/passes/validate")
    @Operation(summary = "驗證 Pass", description = "驗證手動拉入的 Pass 是否有衝突")
    public ResponseEntity<ApiResponse<PassValidationResponse>> validatePass(
            @Valid @RequestBody PassValidationRequest request) {
        PassValidationResponse result = scheduleService.validatePass(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 從衛星需求手動排入 Pass.
     * 
     * @param requestId 需求 ID
     * @param userDetails 當前使用者
     * @return 建立的 Pass
     */
    @PostMapping("/passes/manual-from-request/{requestId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "從需求新增 Pass", description = "將現有的衛星需求強制排入甘特圖")
    public ResponseEntity<ApiResponse<ScheduledPass>> addManualPassFromRequest(
            @PathVariable Long requestId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = 1L; // 實際應從認證資訊中取得
        ScheduledPass pass = scheduleService.addManualPassFromRequest(requestId, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Pass 已從需求手動新增", pass));
    }

    /**
     * 取得可用的排程策略.
     * 
     * @return 策略列表
     */
    @GetMapping("/strategies")
    @Operation(summary = "取得排程策略", description = "取得所有可用的排程策略列表")
    public ResponseEntity<ApiResponse<List<String>>> getAvailableStrategies() {
        List<String> strategies = scheduleService.getAvailableStrategies();
        return ResponseEntity.ok(ApiResponse.success(strategies));
    }
}

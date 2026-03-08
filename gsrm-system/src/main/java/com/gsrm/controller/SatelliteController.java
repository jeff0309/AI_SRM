package com.gsrm.controller;

import com.gsrm.domain.dto.request.SatelliteCreateRequest;
import com.gsrm.domain.dto.response.ApiResponse;
import com.gsrm.domain.entity.Satellite;
import com.gsrm.domain.enums.FrequencyBand;
import com.gsrm.service.SatelliteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 衛星管理控制器.
 *
 * <p>提供衛星 CRUD 操作、緊急任務切換等 REST API。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@RestController
@RequestMapping("/satellites")
@RequiredArgsConstructor
@Tag(name = "Satellite", description = "衛星管理 API")
public class SatelliteController {

    private final SatelliteService satelliteService;

    /**
     * 取得所有衛星（分頁）.
     *
     * @param pageable 分頁參數
     * @return 衛星分頁結果
     */
    @GetMapping
    @Operation(summary = "取得所有衛星", description = "分頁取得所有衛星列表")
    public ResponseEntity<ApiResponse<Page<Satellite>>> getAllSatellites(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(satelliteService.getAllSatellites(pageable)));
    }

    /**
     * 取得已啟用的衛星.
     *
     * @return 衛星列表
     */
    @GetMapping("/enabled")
    @Operation(summary = "取得已啟用衛星", description = "取得所有已啟用的衛星列表")
    public ResponseEntity<ApiResponse<List<Satellite>>> getEnabledSatellites() {
        return ResponseEntity.ok(ApiResponse.success(satelliteService.getEnabledSatellites()));
    }

    /**
     * 取得緊急任務衛星.
     *
     * @return 衛星列表
     */
    @GetMapping("/emergency")
    @Operation(summary = "取得緊急任務衛星", description = "取得所有標記為緊急任務的衛星")
    public ResponseEntity<ApiResponse<List<Satellite>>> getEmergencySatellites() {
        return ResponseEntity.ok(ApiResponse.success(satelliteService.getEmergencySatellites()));
    }

    /**
     * 取得所有公司名稱.
     *
     * @return 公司名稱列表
     */
    @GetMapping("/companies")
    @Operation(summary = "取得公司列表", description = "取得所有不重複的衛星所屬公司名稱")
    public ResponseEntity<ApiResponse<List<String>>> getCompanies() {
        return ResponseEntity.ok(ApiResponse.success(satelliteService.getDistinctCompanies()));
    }

    /**
     * 依 ID 取得衛星.
     *
     * @param id 衛星 ID
     * @return 衛星資料
     */
    @GetMapping("/{id}")
    @Operation(summary = "取得衛星", description = "依 ID 取得單一衛星資料")
    public ResponseEntity<ApiResponse<Satellite>> getSatelliteById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(satelliteService.getSatelliteById(id)));
    }

    /**
     * 依頻段取得衛星.
     *
     * @param band 頻段
     * @return 衛星列表
     */
    @GetMapping("/by-band/{band}")
    @Operation(summary = "依頻段取得衛星", description = "取得指定頻段的衛星列表")
    public ResponseEntity<ApiResponse<List<Satellite>>> getSatellitesByBand(
            @PathVariable FrequencyBand band) {
        return ResponseEntity.ok(
                ApiResponse.success(satelliteService.getSatellitesByFrequencyBand(band)));
    }

    /**
     * 建立新衛星.
     *
     * @param request 衛星建立請求
     * @return 建立的衛星
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "建立衛星", description = "建立新的衛星")
    public ResponseEntity<ApiResponse<Satellite>> createSatellite(
            @Valid @RequestBody SatelliteCreateRequest request) {
        Satellite satellite = satelliteService.createSatellite(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("衛星建立成功", satellite));
    }

    /**
     * 更新衛星資料.
     *
     * @param id      衛星 ID
     * @param request 更新請求
     * @return 更新後的衛星
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "更新衛星", description = "更新現有衛星資料")
    public ResponseEntity<ApiResponse<Satellite>> updateSatellite(
            @PathVariable Long id,
            @Valid @RequestBody SatelliteCreateRequest request) {
        Satellite satellite = satelliteService.updateSatellite(id, request);
        return ResponseEntity.ok(ApiResponse.success("衛星更新成功", satellite));
    }

    /**
     * 刪除衛星.
     *
     * @param id 衛星 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "刪除衛星", description = "刪除指定的衛星")
    public ResponseEntity<ApiResponse<Void>> deleteSatellite(@PathVariable Long id) {
        satelliteService.deleteSatellite(id);
        return ResponseEntity.ok(ApiResponse.success("衛星刪除成功"));
    }

    /**
     * 啟用 / 停用衛星.
     *
     * @param id      衛星 ID
     * @param enabled 是否啟用
     * @return 更新後的衛星
     */
    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "啟用/停用衛星", description = "切換衛星的啟用狀態")
    public ResponseEntity<ApiResponse<Satellite>> setSatelliteEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        Satellite satellite = satelliteService.setSatelliteEnabled(id, enabled);
        return ResponseEntity.ok(ApiResponse.success(enabled ? "衛星已啟用" : "衛星已停用", satellite));
    }

    /**
     * 設定衛星為緊急任務衛星.
     *
     * @param id          衛星 ID
     * @param isEmergency 是否為緊急任務
     * @return 更新後的衛星
     */
    @PatchMapping("/{id}/emergency")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "設定緊急任務", description = "標記或取消標記衛星為緊急任務衛星")
    public ResponseEntity<ApiResponse<Satellite>> setEmergency(
            @PathVariable Long id,
            @RequestParam boolean isEmergency) {
        Satellite satellite = satelliteService.setEmergency(id, isEmergency);
        String message = isEmergency ? "衛星已標記為緊急任務" : "衛星緊急任務標記已取消";
        return ResponseEntity.ok(ApiResponse.success(message, satellite));
    }
}

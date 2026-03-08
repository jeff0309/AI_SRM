package com.gsrm.controller;

import com.gsrm.domain.dto.request.GroundStationRequest;
import com.gsrm.domain.dto.response.ApiResponse;
import com.gsrm.domain.entity.GroundStation;
import com.gsrm.domain.enums.FrequencyBand;
import com.gsrm.service.GroundStationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地面站管理控制器.
 * 
 * <p>提供地面站的 CRUD 操作 REST API。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@RestController
@RequestMapping("/ground-stations")
@Tag(name = "Ground Station", description = "地面站管理 API")
public class GroundStationController {

    private final GroundStationService groundStationService;

    /**
     * 建構地面站控制器.
     * 
     * @param groundStationService 地面站服務
     */
    public GroundStationController(GroundStationService groundStationService) {
        this.groundStationService = groundStationService;
    }

    /**
     * 取得所有地面站（分頁）.
     * 
     * @param pageable 分頁參數
     * @return 地面站分頁結果
     */
    @GetMapping
    @Operation(summary = "取得所有地面站", description = "分頁取得所有地面站列表")
    public ResponseEntity<ApiResponse<Page<GroundStation>>> getAllGroundStations(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GroundStation> stations = groundStationService.getAllGroundStations(pageable);
        return ResponseEntity.ok(ApiResponse.success(stations));
    }

    /**
     * 取得已啟用的地面站.
     * 
     * @return 地面站列表
     */
    @GetMapping("/enabled")
    @Operation(summary = "取得已啟用的地面站", description = "取得所有已啟用的地面站列表")
    public ResponseEntity<ApiResponse<List<GroundStation>>> getEnabledGroundStations() {
        List<GroundStation> stations = groundStationService.getEnabledGroundStations();
        return ResponseEntity.ok(ApiResponse.success(stations));
    }

    /**
     * 依 ID 取得地面站.
     * 
     * @param id 地面站 ID
     * @return 地面站資料
     */
    @GetMapping("/{id}")
    @Operation(summary = "取得地面站", description = "依 ID 取得單一地面站資料")
    public ResponseEntity<ApiResponse<GroundStation>> getGroundStationById(@PathVariable Long id) {
        GroundStation station = groundStationService.getGroundStationById(id);
        return ResponseEntity.ok(ApiResponse.success(station));
    }

    /**
     * 依頻段取得地面站.
     * 
     * @param band 頻段
     * @return 地面站列表
     */
    @GetMapping("/by-band/{band}")
    @Operation(summary = "依頻段取得地面站", description = "取得支援指定頻段的地面站列表")
    public ResponseEntity<ApiResponse<List<GroundStation>>> getGroundStationsByBand(
            @PathVariable FrequencyBand band) {
        List<GroundStation> stations = groundStationService.getGroundStationsByFrequencyBand(band);
        return ResponseEntity.ok(ApiResponse.success(stations));
    }

    /**
     * 建立新的地面站.
     * 
     * @param request 地面站請求
     * @return 建立的地面站
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "建立地面站", description = "建立新的地面站")
    public ResponseEntity<ApiResponse<GroundStation>> createGroundStation(
            @Valid @RequestBody GroundStationRequest request) {
        GroundStation station = groundStationService.createGroundStation(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("地面站建立成功", station));
    }

    /**
     * 更新地面站.
     * 
     * @param id 地面站 ID
     * @param request 更新請求
     * @return 更新後的地面站
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "更新地面站", description = "更新現有地面站資料")
    public ResponseEntity<ApiResponse<GroundStation>> updateGroundStation(
            @PathVariable Long id,
            @Valid @RequestBody GroundStationRequest request) {
        GroundStation station = groundStationService.updateGroundStation(id, request);
        return ResponseEntity.ok(ApiResponse.success("地面站更新成功", station));
    }

    /**
     * 刪除地面站.
     * 
     * @param id 地面站 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "刪除地面站", description = "刪除指定的地面站")
    public ResponseEntity<ApiResponse<Void>> deleteGroundStation(@PathVariable Long id) {
        groundStationService.deleteGroundStation(id);
        return ResponseEntity.ok(ApiResponse.success("地面站刪除成功"));
    }

    /**
     * 啟用/停用地面站.
     * 
     * @param id 地面站 ID
     * @param enabled 是否啟用
     * @return 更新後的地面站
     */
    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "啟用/停用地面站", description = "切換地面站的啟用狀態")
    public ResponseEntity<ApiResponse<GroundStation>> setGroundStationEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        GroundStation station = groundStationService.setGroundStationEnabled(id, enabled);
        String message = enabled ? "地面站已啟用" : "地面站已停用";
        return ResponseEntity.ok(ApiResponse.success(message, station));
    }
}

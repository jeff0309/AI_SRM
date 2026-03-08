package com.gsrm.controller;

import com.gsrm.domain.dto.response.ApiResponse;
import com.gsrm.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排程結果匯出控制器.
 *
 * <p>提供排程結果的 XML（reply.xml）與 CSV 格式下載 API。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@RestController
@RequestMapping("/export")
@RequiredArgsConstructor
@Tag(name = "Export", description = "排程結果匯出 API")
public class ExportController {

    private final ExportService exportService;

    /**
     * 取得支援的匯出格式.
     *
     * @return 格式列表
     */
    @GetMapping("/formats")
    @Operation(summary = "取得支援格式", description = "取得所有支援的排程結果匯出格式")
    public ResponseEntity<ApiResponse<List<String>>> getSupportedFormats() {
        return ResponseEntity.ok(ApiResponse.success(exportService.getSupportedFormats()));
    }

    /**
     * 匯出排程結果（XML 格式）.
     *
     * <p>回應為附件下載（Content-Disposition: attachment）。
     * 輸出檔案格式為 reply.xml。</p>
     *
     * @param sessionId 排程 Session ID
     * @param response  HTTP 回應
     */
    @GetMapping("/sessions/{sessionId}/xml")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR') or hasRole('VIEWER')")
    @Operation(summary = "匯出 XML", description = "將指定 Session 的排程結果匯出為 XML 格式")
    public void exportXml(@PathVariable Long sessionId, HttpServletResponse response) {
        exportService.exportScheduleResult(sessionId, "XML", response);
    }

    /**
     * 匯出排程結果（CSV 格式）.
     *
     * <p>回應為附件下載（Content-Disposition: attachment）。</p>
     *
     * @param sessionId 排程 Session ID
     * @param response  HTTP 回應
     */
    @GetMapping("/sessions/{sessionId}/csv")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR') or hasRole('VIEWER')")
    @Operation(summary = "匯出 CSV", description = "將指定 Session 的排程結果匯出為 CSV 格式")
    public void exportCsv(@PathVariable Long sessionId, HttpServletResponse response) {
        exportService.exportScheduleResult(sessionId, "CSV", response);
    }

    /**
     * 依格式參數匯出排程結果（泛用端點）.
     *
     * @param sessionId 排程 Session ID
     * @param format    匯出格式（"XML" 或 "CSV"，不分大小寫）
     * @param response  HTTP 回應
     */
    @GetMapping("/sessions/{sessionId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR') or hasRole('VIEWER')")
    @Operation(summary = "匯出排程結果", description = "依格式參數匯出指定 Session 的排程結果")
    public void exportByFormat(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "XML") String format,
            HttpServletResponse response) {
        exportService.exportScheduleResult(sessionId, format, response);
    }
}

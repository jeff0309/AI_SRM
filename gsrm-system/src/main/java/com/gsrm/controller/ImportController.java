package com.gsrm.controller;

import com.gsrm.domain.dto.response.ApiResponse;
import com.gsrm.domain.entity.SatelliteRequest;
import com.gsrm.domain.entity.StationUnavailability;
import com.gsrm.service.ImportService;
import com.gsrm.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 檔案匯入控制器.
 *
 * <p>提供衛星需求（XML/CSV）與地面站維護時段（TXT）的檔案上傳匯入 API。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@RestController
@RequestMapping("/import")
@RequiredArgsConstructor
@Tag(name = "Import", description = "檔案匯入 API")
public class ImportController {

    private final ImportService importService;
    private final UserService   userService;

    /**
     * 取得支援的匯入格式.
     *
     * @return 格式列表
     */
    @GetMapping("/formats")
    @Operation(summary = "取得支援格式", description = "取得所有支援的需求檔案匯入格式")
    public ResponseEntity<ApiResponse<List<String>>> getSupportedFormats() {
        return ResponseEntity.ok(ApiResponse.success(importService.getSupportedRequestFormats()));
    }

    /**
     * 匯入衛星需求檔案.
     *
     * <p>支援 XML（依 XSD 規範）與 CSV 格式。</p>
     *
     * @param file           上傳的檔案
     * @param sessionId      目標排程 Session ID
     * @param authentication 當前登入資訊
     * @return 匯入的需求列表
     */
    @PostMapping(value = "/satellite-requests", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "匯入衛星需求", description = "上傳 XML 或 CSV 格式的衛星需求檔案")
    public ResponseEntity<ApiResponse<List<SatelliteRequest>>> importSatelliteRequests(
            @RequestPart("file") MultipartFile file,
            @RequestParam Long sessionId,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<SatelliteRequest> requests = importService.importSatelliteRequests(file, sessionId, userId);
        return ResponseEntity.ok(ApiResponse.success(
                "成功匯入 " + requests.size() + " 筆衛星需求", requests));
    }

    /**
     * 匯入地面站維護時段檔案.
     *
     * <p>支援純文字格式：每行「地面站名稱, 開始時間, 結束時間」。</p>
     *
     * @param file           上傳的 TXT 檔案
     * @param authentication 當前登入資訊
     * @return 匯入的維護時段列表
     */
    @PostMapping(value = "/station-unavailabilities", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @Operation(summary = "匯入維護時段", description = "上傳 TXT 格式的地面站維護時段檔案")
    public ResponseEntity<ApiResponse<List<StationUnavailability>>> importStationUnavailabilities(
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<StationUnavailability> unavailabilities =
                importService.importStationUnavailabilities(file, userId);
        return ResponseEntity.ok(ApiResponse.success(
                "成功匯入 " + unavailabilities.size() + " 筆維護時段", unavailabilities));
    }

    /**
     * 從 Authentication 中取得使用者 ID.
     *
     * @param authentication Spring Security 認證物件
     * @return 使用者 ID
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication == null) return null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails ud) {
            return userService.getUserByUsername(ud.getUsername()).getId();
        }
        return null;
    }
}

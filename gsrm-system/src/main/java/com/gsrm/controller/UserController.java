package com.gsrm.controller;

import com.gsrm.domain.dto.request.UserCreateRequest;
import com.gsrm.domain.dto.response.ApiResponse;
import com.gsrm.domain.entity.User;
import com.gsrm.domain.enums.UserRole;
import com.gsrm.service.UserService;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 使用者管理控制器.
 *
 * <p>提供使用者帳號 CRUD、鎖定/停用、密碼管理等 REST API。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "使用者管理 API")
public class UserController {

    private final UserService userService;

    /**
     * 取得所有使用者（分頁）.
     *
     * @param pageable 分頁參數
     * @return 使用者分頁結果
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "取得所有使用者", description = "管理員分頁取得所有使用者列表")
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers(pageable)));
    }

    /**
     * 依 ID 取得使用者.
     *
     * @param id 使用者 ID
     * @return 使用者資料
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @Operation(summary = "取得使用者", description = "依 ID 取得單一使用者資料")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    /**
     * 依角色取得使用者.
     *
     * @param role 角色
     * @return 使用者列表
     */
    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "依角色取得使用者", description = "取得指定角色的所有使用者")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByRole(@PathVariable UserRole role) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUsersByRole(role)));
    }

    /**
     * 建立新使用者.
     *
     * @param request        使用者建立請求
     * @param authentication 當前登入資訊
     * @return 建立的使用者
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "建立使用者", description = "管理員建立新使用者帳號")
    public ResponseEntity<ApiResponse<User>> createUser(
            @Valid @RequestBody UserCreateRequest request,
            Authentication authentication) {
        // 取得當前操作者 ID（從 JWT Principal）
        Long createdBy = extractUserId(authentication);
        User user = userService.createUser(request, createdBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("使用者建立成功", user));
    }

    /**
     * 更新使用者資料.
     *
     * @param id      使用者 ID
     * @param request 更新請求
     * @return 更新後的使用者
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新使用者", description = "管理員更新使用者資料")
    public ResponseEntity<ApiResponse<User>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserCreateRequest request) {
        User user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("使用者更新成功", user));
    }

    /**
     * 刪除使用者.
     *
     * @param id 使用者 ID
     * @return 刪除結果
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "刪除使用者", description = "管理員刪除指定使用者帳號")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("使用者刪除成功"));
    }

    /**
     * 鎖定 / 解鎖使用者帳號.
     *
     * @param id     使用者 ID
     * @param locked 是否鎖定
     * @return 更新後的使用者
     */
    @PatchMapping("/{id}/locked")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "鎖定/解鎖帳號", description = "切換使用者帳號鎖定狀態")
    public ResponseEntity<ApiResponse<User>> setUserLocked(
            @PathVariable Long id,
            @RequestParam boolean locked) {
        User user = userService.setUserLocked(id, locked);
        return ResponseEntity.ok(ApiResponse.success(locked ? "帳號已鎖定" : "帳號已解鎖", user));
    }

    /**
     * 啟用 / 停用使用者帳號.
     *
     * @param id      使用者 ID
     * @param enabled 是否啟用
     * @return 更新後的使用者
     */
    @PatchMapping("/{id}/enabled")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "啟用/停用帳號", description = "切換使用者帳號啟用狀態")
    public ResponseEntity<ApiResponse<User>> setUserEnabled(
            @PathVariable Long id,
            @RequestParam boolean enabled) {
        User user = userService.setUserEnabled(id, enabled);
        return ResponseEntity.ok(ApiResponse.success(enabled ? "帳號已啟用" : "帳號已停用", user));
    }

    /**
     * 修改密碼（使用者自行操作）.
     *
     * @param id      使用者 ID
     * @param payload 包含 oldPassword / newPassword 的 Map
     * @return 操作結果
     */
    @PostMapping("/{id}/change-password")
    @Operation(summary = "修改密碼", description = "使用者自行修改密碼")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        userService.changePassword(id, payload.get("oldPassword"), payload.get("newPassword"));
        return ResponseEntity.ok(ApiResponse.success("密碼修改成功"));
    }

    /**
     * 管理員強制重設密碼.
     *
     * @param id      使用者 ID
     * @param payload 包含 newPassword 的 Map
     * @return 操作結果
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "重設密碼", description = "管理員強制重設指定使用者的密碼")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        userService.resetPassword(id, payload.get("newPassword"));
        return ResponseEntity.ok(ApiResponse.success("密碼重設成功"));
    }

    /**
     * 從 Authentication 物件中擷取使用者 ID.
     *
     * @param authentication Spring Security 認證物件
     * @return 使用者 ID，若無法取得則回傳 null
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        try {
            // 假設 Principal 實作了 getId() 方法（CustomUserDetails）
            Object principal = authentication.getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
                return userService.getUserByUsername(ud.getUsername()).getId();
            }
        } catch (Exception ignored) {
            // 無法解析則回傳 null
        }
        return null;
    }
}

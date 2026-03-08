package com.gsrm.controller;

import com.gsrm.domain.dto.request.LoginRequest;
import com.gsrm.domain.dto.response.ApiResponse;
import com.gsrm.domain.dto.response.LoginResponse;
import com.gsrm.domain.entity.User;
import com.gsrm.repository.UserRepository;
import com.gsrm.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 認證控制器.
 * 
 * <p>處理使用者登入、登出等認證相關操作。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "使用者認證 API")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;

    /**
     * 建構認證控制器.
     * 
     * @param authenticationManager 認證管理器
     * @param tokenProvider JWT Token 提供者
     * @param userRepository 使用者 Repository
     */
    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    /**
     * 使用者登入.
     * 
     * @param request 登入請求
     * @return 登入回應（含 JWT Token）
     */
    @PostMapping("/login")
    @Operation(summary = "使用者登入", description = "驗證使用者憑證並回傳 JWT Token")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        // 執行認證
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 產生 Token
        String token = tokenProvider.generateToken(authentication);

        // 取得使用者資訊
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("使用者不存在"));

        // 更新最後登入時間
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 建立回應
        LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getExpirationInSeconds())
                .userId(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .build();

        return ResponseEntity.ok(ApiResponse.success("登入成功", loginResponse));
    }

    /**
     * 驗證 Token 有效性.
     * 
     * @return 驗證結果
     */
    @GetMapping("/validate")
    @Operation(summary = "驗證 Token", description = "檢查當前 Token 是否有效")
    public ResponseEntity<ApiResponse<String>> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return ResponseEntity.ok(ApiResponse.success("Token 有效", authentication.getName()));
        }
        return ResponseEntity.ok(ApiResponse.error("Token 無效或已過期"));
    }

    /**
     * 使用者登出.
     * 
     * @return 登出結果
     */
    @PostMapping("/logout")
    @Operation(summary = "使用者登出", description = "清除當前 Session")
    public ResponseEntity<ApiResponse<Void>> logout() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.success("登出成功"));
    }
}

package com.gsrm.domain.dto.response;

import com.gsrm.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登入回應 DTO.
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * JWT Access Token.
     */
    private String token;

    /**
     * Token 類型.
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Token 過期時間（秒）.
     */
    private Long expiresIn;

    /**
     * 使用者 ID.
     */
    private Long userId;

    /**
     * 使用者名稱.
     */
    private String username;

    /**
     * 顯示名稱.
     */
    private String displayName;

    /**
     * 使用者角色.
     */
    private UserRole role;
}

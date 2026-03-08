package com.gsrm.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登入請求 DTO.
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * 使用者名稱或電子郵件.
     */
    @NotBlank(message = "使用者名稱不可為空")
    private String username;

    /**
     * 密碼.
     */
    @NotBlank(message = "密碼不可為空")
    private String password;
}

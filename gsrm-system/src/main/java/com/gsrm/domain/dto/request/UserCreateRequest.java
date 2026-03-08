package com.gsrm.domain.dto.request;

import com.gsrm.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 建立使用者請求 DTO.
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    /**
     * 使用者名稱.
     */
    @NotBlank(message = "使用者名稱不可為空")
    @Size(min = 3, max = 50, message = "使用者名稱長度須介於 3-50 字元")
    private String username;

    /**
     * 密碼.
     */
    @NotBlank(message = "密碼不可為空")
    @Size(min = 6, max = 100, message = "密碼長度須介於 6-100 字元")
    private String password;

    /**
     * 電子郵件.
     */
    @NotBlank(message = "電子郵件不可為空")
    @Email(message = "電子郵件格式不正確")
    private String email;

    /**
     * 顯示名稱.
     */
    @Size(max = 100, message = "顯示名稱不可超過 100 字元")
    private String displayName;

    /**
     * 使用者角色.
     */
    @NotNull(message = "角色不可為空")
    private UserRole role;
}

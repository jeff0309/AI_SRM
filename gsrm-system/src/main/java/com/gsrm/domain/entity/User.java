package com.gsrm.domain.entity;

import com.gsrm.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 使用者實體類別.
 * 
 * <p>代表系統中的使用者帳號，包含身分驗證與權限資訊。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "username"),
    @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * 使用者唯一識別碼.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 使用者帳號名稱.
     */
    @Column(name = "username", nullable = false, length = 50)
    private String username;

    /**
     * 使用者密碼（加密後儲存）.
     */
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * 使用者電子郵件.
     */
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    /**
     * 使用者顯示名稱.
     */
    @Column(name = "display_name", length = 100)
    private String displayName;

    /**
     * 使用者角色.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role;

    /**
     * 帳號是否啟用.
     */
    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * 帳號是否被鎖定.
     */
    @Column(name = "locked", nullable = false)
    @Builder.Default
    private Boolean locked = false;

    /**
     * 最後登入時間.
     */
    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    /**
     * 建立時間.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 最後更新時間.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 建立使用者的管理員 ID.
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 檢查使用者是否可登入.
     * 
     * @return 如果帳號已啟用且未被鎖定則回傳 true
     */
    public boolean canLogin() {
        return Boolean.TRUE.equals(enabled) && Boolean.FALSE.equals(locked);
    }

    /**
     * 檢查是否具有管理員權限.
     * 
     * @return 如果是管理員角色則回傳 true
     */
    public boolean isAdmin() {
        return role != null && role.hasAdminAccess();
    }

    /**
     * 檢查是否具有操作員權限.
     * 
     * @return 如果具有操作員以上權限則回傳 true
     */
    public boolean isOperator() {
        return role != null && role.hasOperatorAccess();
    }
}

package com.gsrm.service;

import com.gsrm.domain.dto.request.UserCreateRequest;
import com.gsrm.domain.entity.User;
import com.gsrm.domain.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 使用者服務介面.
 *
 * <p>定義使用者帳號管理的業務邏輯，包含 CRUD、鎖定、角色管理。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
public interface UserService {

    /**
     * 建立新使用者.
     *
     * @param request    建立請求
     * @param createdBy  建立者 ID
     * @return 建立完成的使用者
     */
    User createUser(UserCreateRequest request, Long createdBy);

    /**
     * 更新使用者資料.
     *
     * @param id      使用者 ID
     * @param request 更新請求
     * @return 更新後的使用者
     */
    User updateUser(Long id, UserCreateRequest request);

    /**
     * 依 ID 取得使用者.
     *
     * @param id 使用者 ID
     * @return 使用者實體
     */
    User getUserById(Long id);

    /**
     * 依使用者名稱取得使用者.
     *
     * @param username 使用者名稱
     * @return 使用者實體
     */
    User getUserByUsername(String username);

    /**
     * 取得所有使用者（分頁）.
     *
     * @param pageable 分頁資訊
     * @return 使用者分頁結果
     */
    Page<User> getAllUsers(Pageable pageable);

    /**
     * 依角色取得使用者列表.
     *
     * @param role 使用者角色
     * @return 使用者列表
     */
    List<User> getUsersByRole(UserRole role);

    /**
     * 刪除使用者.
     *
     * @param id 使用者 ID
     */
    void deleteUser(Long id);

    /**
     * 鎖定 / 解鎖使用者帳號.
     *
     * @param id     使用者 ID
     * @param locked {@code true} 表示鎖定
     * @return 更新後的使用者
     */
    User setUserLocked(Long id, boolean locked);

    /**
     * 啟用 / 停用使用者帳號.
     *
     * @param id      使用者 ID
     * @param enabled {@code true} 表示啟用
     * @return 更新後的使用者
     */
    User setUserEnabled(Long id, boolean enabled);

    /**
     * 修改使用者密碼.
     *
     * @param id          使用者 ID
     * @param oldPassword 舊密碼（明文）
     * @param newPassword 新密碼（明文）
     */
    void changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 管理員強制重設密碼.
     *
     * @param id          使用者 ID
     * @param newPassword 新密碼（明文）
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 更新最後登入時間.
     *
     * @param username 使用者名稱
     */
    void updateLastLoginTime(String username);

    /**
     * 檢查使用者名稱是否已存在.
     *
     * @param username 使用者名稱
     * @return {@code true} 表示已存在
     */
    boolean existsByUsername(String username);

    /**
     * 檢查電子郵件是否已存在.
     *
     * @param email 電子郵件
     * @return {@code true} 表示已存在
     */
    boolean existsByEmail(String email);
}

package com.gsrm.repository;

import com.gsrm.domain.entity.User;
import com.gsrm.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 使用者資料存取介面.
 * 
 * <p>提供使用者實體的 CRUD 操作與自訂查詢方法。
 * 使用 Spring Data JPA 的 Repository Pattern。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 依使用者名稱查詢.
     * 
     * @param username 使用者名稱
     * @return 使用者 Optional
     */
    Optional<User> findByUsername(String username);

    /**
     * 依電子郵件查詢.
     * 
     * @param email 電子郵件
     * @return 使用者 Optional
     */
    Optional<User> findByEmail(String email);

    /**
     * 檢查使用者名稱是否存在.
     * 
     * @param username 使用者名稱
     * @return 如果存在則回傳 true
     */
    boolean existsByUsername(String username);

    /**
     * 檢查電子郵件是否存在.
     * 
     * @param email 電子郵件
     * @return 如果存在則回傳 true
     */
    boolean existsByEmail(String email);

    /**
     * 依角色查詢使用者列表.
     * 
     * @param role 使用者角色
     * @return 使用者列表
     */
    List<User> findByRole(UserRole role);

    /**
     * 查詢已啟用的使用者.
     * 
     * @return 已啟用的使用者列表
     */
    List<User> findByEnabledTrue();

    /**
     * 查詢已鎖定的使用者.
     * 
     * @return 已鎖定的使用者列表
     */
    List<User> findByLockedTrue();

    /**
     * 查詢最近登入的使用者.
     * 
     * @param since 起始時間
     * @return 使用者列表
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :since ORDER BY u.lastLoginAt DESC")
    List<User> findRecentlyLoggedIn(@Param("since") LocalDateTime since);

    /**
     * 計算各角色的使用者數量.
     * 
     * @param role 使用者角色
     * @return 使用者數量
     */
    long countByRole(UserRole role);

    /**
     * 依使用者名稱或電子郵件查詢（用於登入）.
     * 
     * @param username 使用者名稱
     * @param email 電子郵件
     * @return 使用者 Optional
     */
    @Query("SELECT u FROM User u WHERE u.username = :username OR u.email = :email")
    Optional<User> findByUsernameOrEmail(@Param("username") String username, 
                                          @Param("email") String email);
}

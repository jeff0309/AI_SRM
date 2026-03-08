package com.gsrm.security;

import com.gsrm.domain.entity.User;
import com.gsrm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 自訂 UserDetailsService 實作.
 *
 * <p>供 Spring Security 在認證流程中載入使用者資訊。
 * 同時支援以使用者名稱或電子郵件進行查詢。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 依使用者名稱（或電子郵件）載入使用者.
     *
     * <p>Spring Security 會在登入驗證時呼叫此方法。
     * 本實作允許使用者以帳號或電子郵件登入。</p>
     *
     * @param username 使用者名稱或電子郵件
     * @return Spring Security UserDetails 物件
     * @throws UsernameNotFoundException 當找不到使用者時拋出
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> {
                    log.warn("[Security] 找不到使用者：{}", username);
                    return new UsernameNotFoundException("找不到使用者：" + username);
                });

        if (!user.canLogin()) {
            log.warn("[Security] 使用者 {} 帳號已停用或被鎖定", username);
            throw new UsernameNotFoundException("帳號已停用或被鎖定：" + username);
        }

        log.debug("[Security] 載入使用者：{}，角色：{}", user.getUsername(), user.getRole());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .accountExpired(false)
                .accountLocked(Boolean.TRUE.equals(user.getLocked()))
                .credentialsExpired(false)
                .disabled(!Boolean.TRUE.equals(user.getEnabled()))
                .build();
    }

    /**
     * 依 ID 載入使用者（供 JWT 過濾器使用）.
     *
     * @param userId 使用者 ID
     * @return Spring Security UserDetails 物件
     * @throws UsernameNotFoundException 當找不到使用者時拋出
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long userId) throws UsernameNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("找不到使用者 ID：" + userId));
        return loadUserByUsername(user.getUsername());
    }
}

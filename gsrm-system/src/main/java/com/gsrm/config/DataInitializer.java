package com.gsrm.config;

import com.gsrm.domain.entity.User;
import com.gsrm.domain.enums.UserRole;
import com.gsrm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 應用程式啟動時的資料初始化器.
 *
 * <p>確保預設使用者帳號存在。
 * 使用 ApplicationRunner 在 Spring Context 完全啟動後執行，
 * 確保 PasswordEncoder 等 Bean 都已就緒。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createUserIfNotExists("admin",    "admin123",    "admin@gsrm.com",    "System Administrator", UserRole.ADMIN);
        createUserIfNotExists("operator", "operator123", "operator@gsrm.com", "Schedule Operator",    UserRole.OPERATOR);
        createUserIfNotExists("viewer",   "viewer123",   "viewer@gsrm.com",   "Read-only Viewer",     UserRole.VIEWER);
    }

    private void createUserIfNotExists(String username, String rawPassword,
                                        String email, String displayName, UserRole role) {
        if (userRepository.existsByUsername(username)) {
            // 帳號已存在，強制更新密碼確保正確
            userRepository.findByUsername(username).ifPresent(user -> {
                user.setPassword(passwordEncoder.encode(rawPassword));
                user.setEnabled(true);
                user.setLocked(false);
                userRepository.save(user);
                log.info("[DataInitializer] 更新帳號密碼：{}", username);
            });
            return;
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .email(email)
                .displayName(displayName)
                .role(role)
                .enabled(true)
                .locked(false)
                .build();

        userRepository.save(user);
        log.info("[DataInitializer] 建立預設帳號：{} ({})", username, role);
    }
}

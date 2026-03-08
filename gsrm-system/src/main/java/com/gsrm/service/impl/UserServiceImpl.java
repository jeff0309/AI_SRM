package com.gsrm.service.impl;

import com.gsrm.domain.dto.request.UserCreateRequest;
import com.gsrm.domain.entity.User;
import com.gsrm.domain.enums.UserRole;
import com.gsrm.exception.ConflictException;
import com.gsrm.exception.ResourceNotFoundException;
import com.gsrm.exception.ValidationException;
import com.gsrm.repository.UserRepository;
import com.gsrm.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 使用者服務實作.
 *
 * <p>涵蓋使用者帳號的 CRUD、鎖定、密碼管理等業務邏輯。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    /* ─────────── CRUD ─────────── */

    /** {@inheritDoc} */
    @Override
    @Transactional
    public User createUser(UserCreateRequest request, Long createdBy) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("使用者", "username", request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("使用者", "email", request.getEmail());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .displayName(request.getDisplayName())
                .role(request.getRole())
                .enabled(true)
                .locked(false)
                .createdBy(createdBy)
                .build();

        User saved = userRepository.save(user);
        log.info("[UserService] 建立使用者：{}，角色：{}", saved.getUsername(), saved.getRole());
        return saved;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public User updateUser(Long id, UserCreateRequest request) {
        User user = getUserById(id);

        // 使用者名稱變更時，檢查是否重複
        if (!user.getUsername().equals(request.getUsername())
                && userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("使用者", "username", request.getUsername());
        }
        // 電子郵件變更時，檢查是否重複
        if (!user.getEmail().equals(request.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("使用者", "email", request.getEmail());
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDisplayName(request.getDisplayName());
        user.setRole(request.getRole());
        // 若請求中帶入新密碼才更新
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);
        log.info("[UserService] 更新使用者：{}", saved.getUsername());
        return saved;
    }

    /** {@inheritDoc} */
    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("使用者", id));
    }

    /** {@inheritDoc} */
    @Override
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("使用者", "username", username));
    }

    /** {@inheritDoc} */
    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /** {@inheritDoc} */
    @Override
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
        log.info("[UserService] 刪除使用者：{}", user.getUsername());
    }

    /* ─────────── 帳號狀態管理 ─────────── */

    /** {@inheritDoc} */
    @Override
    @Transactional
    public User setUserLocked(Long id, boolean locked) {
        User user = getUserById(id);
        user.setLocked(locked);
        User saved = userRepository.save(user);
        log.info("[UserService] 使用者 {} 鎖定狀態設為：{}", user.getUsername(), locked);
        return saved;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public User setUserEnabled(Long id, boolean enabled) {
        User user = getUserById(id);
        user.setEnabled(enabled);
        User saved = userRepository.save(user);
        log.info("[UserService] 使用者 {} 啟用狀態設為：{}", user.getUsername(), enabled);
        return saved;
    }

    /* ─────────── 密碼管理 ─────────── */

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = getUserById(id);

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ValidationException("舊密碼不正確");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("新密碼長度須至少 6 個字元");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("[UserService] 使用者 {} 已修改密碼", user.getUsername());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        User user = getUserById(id);

        if (newPassword == null || newPassword.length() < 6) {
            throw new ValidationException("新密碼長度須至少 6 個字元");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("[UserService] 管理員重設使用者 {} 的密碼", user.getUsername());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void updateLastLoginTime(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }

    /* ─────────── 查詢輔助 ─────────── */

    /** {@inheritDoc} */
    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}

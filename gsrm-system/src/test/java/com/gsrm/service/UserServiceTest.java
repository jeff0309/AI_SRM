package com.gsrm.service;

import com.gsrm.domain.dto.request.UserCreateRequest;
import com.gsrm.domain.entity.User;
import com.gsrm.domain.enums.UserRole;
import com.gsrm.exception.ConflictException;
import com.gsrm.exception.ResourceNotFoundException;
import com.gsrm.exception.ValidationException;
import com.gsrm.repository.UserRepository;
import com.gsrm.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * UserServiceImpl 單元測試.
 *
 * @author Jeff
 * @since 2026-03-06
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 測試")
class UserServiceTest {

    @Mock
    private UserRepository  userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User sampleUser;
    private UserCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .email("test@example.com")
                .role(UserRole.OPERATOR)
                .enabled(true)
                .locked(false)
                .build();

        createRequest = UserCreateRequest.builder()
                .username("newuser")
                .password("password123")
                .email("new@example.com")
                .displayName("New User")
                .role(UserRole.VIEWER)
                .build();
    }

    /* ─────────── createUser ─────────── */

    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        @Test
        @DisplayName("正常建立使用者")
        void shouldCreateUserSuccessfully() {
            given(userRepository.existsByUsername("newuser")).willReturn(false);
            given(userRepository.existsByEmail("new@example.com")).willReturn(false);
            given(passwordEncoder.encode("password123")).willReturn("hashedPwd");
            given(userRepository.save(any(User.class))).willAnswer(inv -> {
                User u = inv.getArgument(0);
                u.setId(2L);
                return u;
            });

            User result = userService.createUser(createRequest, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("newuser");
            assertThat(result.getEmail()).isEqualTo("new@example.com");
            assertThat(result.getRole()).isEqualTo(UserRole.VIEWER);
        }

        @Test
        @DisplayName("使用者名稱重複時拋出 ConflictException")
        void shouldThrowConflictWhenUsernameExists() {
            given(userRepository.existsByUsername("newuser")).willReturn(true);

            assertThatThrownBy(() -> userService.createUser(createRequest, 1L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("newuser");
        }

        @Test
        @DisplayName("電子郵件重複時拋出 ConflictException")
        void shouldThrowConflictWhenEmailExists() {
            given(userRepository.existsByUsername("newuser")).willReturn(false);
            given(userRepository.existsByEmail("new@example.com")).willReturn(true);

            assertThatThrownBy(() -> userService.createUser(createRequest, 1L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("new@example.com");
        }
    }

    /* ─────────── getUserById ─────────── */

    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("存在時正確回傳")
        void shouldReturnUserWhenExists() {
            given(userRepository.findById(1L)).willReturn(Optional.of(sampleUser));

            User result = userService.getUserById(1L);

            assertThat(result.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("不存在時拋出 ResourceNotFoundException")
        void shouldThrowWhenNotFound() {
            given(userRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    /* ─────────── setUserLocked ─────────── */

    @Nested
    @DisplayName("setUserLocked()")
    class SetUserLocked {

        @Test
        @DisplayName("成功鎖定帳號")
        void shouldLockUser() {
            given(userRepository.findById(1L)).willReturn(Optional.of(sampleUser));
            given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

            User result = userService.setUserLocked(1L, true);

            assertThat(result.getLocked()).isTrue();
        }
    }

    /* ─────────── changePassword ─────────── */

    @Nested
    @DisplayName("changePassword()")
    class ChangePassword {

        @Test
        @DisplayName("舊密碼正確時成功修改")
        void shouldChangePasswordSuccessfully() {
            given(userRepository.findById(1L)).willReturn(Optional.of(sampleUser));
            given(passwordEncoder.matches("oldPass", "encodedPassword")).willReturn(true);
            given(passwordEncoder.encode("newPass123")).willReturn("newEncodedPwd");
            given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

            assertThatNoException().isThrownBy(
                    () -> userService.changePassword(1L, "oldPass", "newPass123"));
        }

        @Test
        @DisplayName("舊密碼錯誤時拋出 ValidationException")
        void shouldThrowWhenOldPasswordIncorrect() {
            given(userRepository.findById(1L)).willReturn(Optional.of(sampleUser));
            given(passwordEncoder.matches("wrongPass", "encodedPassword")).willReturn(false);

            assertThatThrownBy(() -> userService.changePassword(1L, "wrongPass", "newPass123"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("舊密碼");
        }

        @Test
        @DisplayName("新密碼太短時拋出 ValidationException")
        void shouldThrowWhenNewPasswordTooShort() {
            given(userRepository.findById(1L)).willReturn(Optional.of(sampleUser));
            given(passwordEncoder.matches("oldPass", "encodedPassword")).willReturn(true);

            assertThatThrownBy(() -> userService.changePassword(1L, "oldPass", "ab"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("6");
        }
    }
}

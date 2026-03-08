package com.gsrm.util;

import com.gsrm.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * ValidationUtils 單元測試.
 *
 * @author Jeff
 * @since 2026-03-06
 */
@DisplayName("ValidationUtils 測試")
class ValidationUtilsTest {

    @Test
    @DisplayName("requireNonNull() 值為 null 時拋出")
    void shouldThrowWhenNull() {
        assertThatThrownBy(() -> ValidationUtils.requireNonNull(null, "testField"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("testField");
    }

    @Test
    @DisplayName("requireNonNull() 值不為 null 時不拋出")
    void shouldNotThrowWhenNotNull() {
        assertThatNoException().isThrownBy(
                () -> ValidationUtils.requireNonNull("value", "testField"));
    }

    @Test
    @DisplayName("requireNonBlank() 空白字串拋出")
    void shouldThrowWhenBlank() {
        assertThatThrownBy(() -> ValidationUtils.requireNonBlank("  ", "name"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ValidationUtils.requireNonBlank("", "name"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ValidationUtils.requireNonBlank(null, "name"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("requireNonEmpty() 空集合拋出")
    void shouldThrowWhenEmpty() {
        assertThatThrownBy(() -> ValidationUtils.requireNonEmpty(Collections.emptyList(), "list"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("requireNonEmpty() 非空集合不拋出")
    void shouldNotThrowWhenNotEmpty() {
        assertThatNoException().isThrownBy(
                () -> ValidationUtils.requireNonEmpty(List.of("item"), "list"));
    }

    @Test
    @DisplayName("requireInRange() 超出範圍拋出")
    void shouldThrowWhenOutOfRange() {
        assertThatThrownBy(() -> ValidationUtils.requireInRange(150, 1, 100, "priority"))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ValidationUtils.requireInRange(0, 1, 100, "priority"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("requireInRange() 邊界值不拋出")
    void shouldNotThrowAtBoundary() {
        assertThatNoException().isThrownBy(
                () -> ValidationUtils.requireInRange(1, 1, 100, "priority"));
        assertThatNoException().isThrownBy(
                () -> ValidationUtils.requireInRange(100, 1, 100, "priority"));
    }

    @Test
    @DisplayName("requireValidTimeRange() 結束時間早於開始時間時拋出")
    void shouldThrowWhenInvalidTimeRange() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end   = start.minusHours(1);

        assertThatThrownBy(() -> ValidationUtils.requireValidTimeRange(start, end, "排程"))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("結束時間");
    }

    @Test
    @DisplayName("requireValidEmail() 格式不正確時拋出")
    void shouldThrowOnInvalidEmail() {
        assertThatThrownBy(() -> ValidationUtils.requireValidEmail("not-an-email", "email"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("requireValidEmail() 正確格式不拋出")
    void shouldNotThrowOnValidEmail() {
        assertThatNoException().isThrownBy(
                () -> ValidationUtils.requireValidEmail("test@example.com", "email"));
    }

    @Test
    @DisplayName("requireValidLongitude() 超出範圍拋出")
    void shouldThrowOnInvalidLongitude() {
        assertThatThrownBy(() -> ValidationUtils.requireValidLongitude(200.0))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("requireValidLatitude() 超出範圍拋出")
    void shouldThrowOnInvalidLatitude() {
        assertThatThrownBy(() -> ValidationUtils.requireValidLatitude(-100.0))
                .isInstanceOf(ValidationException.class);
    }
}

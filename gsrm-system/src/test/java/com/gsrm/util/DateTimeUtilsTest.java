package com.gsrm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * DateTimeUtils 單元測試.
 *
 * @author Jeff
 * @since 2026-03-06
 */
@DisplayName("DateTimeUtils 測試")
class DateTimeUtilsTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2026, 3, 6, 10, 0, 0);

    @Test
    @DisplayName("toIsoString() 格式正確")
    void shouldFormatToIsoString() {
        assertThat(DateTimeUtils.toIsoString(BASE)).isEqualTo("2026-03-06T10:00:00");
    }

    @Test
    @DisplayName("toIsoString(null) 回傳空字串")
    void shouldReturnEmptyStringForNull() {
        assertThat(DateTimeUtils.toIsoString(null)).isEmpty();
    }

    @Test
    @DisplayName("toDisplayString() 格式正確")
    void shouldFormatToDisplayString() {
        assertThat(DateTimeUtils.toDisplayString(BASE)).isEqualTo("2026-03-06 10:00:00");
    }

    @Test
    @DisplayName("toDisplayString(null) 回傳 -")
    void shouldReturnDashForNull() {
        assertThat(DateTimeUtils.toDisplayString(null)).isEqualTo("-");
    }

    @Test
    @DisplayName("secondsBetween() 計算正確")
    void shouldCalculateSecondsBetween() {
        LocalDateTime end = BASE.plusMinutes(10);
        assertThat(DateTimeUtils.secondsBetween(BASE, end)).isEqualTo(600L);
    }

    @Test
    @DisplayName("secondsBetween() 回傳絕對值")
    void shouldReturnAbsoluteSeconds() {
        LocalDateTime earlier = BASE.minusMinutes(5);
        assertThat(DateTimeUtils.secondsBetween(BASE, earlier)).isEqualTo(300L);
    }

    @Test
    @DisplayName("isOverlapping() 重疊判斷正確")
    void shouldDetectOverlap() {
        LocalDateTime aStart = BASE;
        LocalDateTime aEnd   = BASE.plusMinutes(20);
        LocalDateTime bStart = BASE.plusMinutes(10);
        LocalDateTime bEnd   = BASE.plusMinutes(30);

        assertThat(DateTimeUtils.isOverlapping(aStart, aEnd, bStart, bEnd)).isTrue();
    }

    @Test
    @DisplayName("isOverlapping() 不重疊判斷正確")
    void shouldDetectNonOverlap() {
        LocalDateTime aStart = BASE;
        LocalDateTime aEnd   = BASE.plusMinutes(10);
        LocalDateTime bStart = BASE.plusMinutes(15);
        LocalDateTime bEnd   = BASE.plusMinutes(25);

        assertThat(DateTimeUtils.isOverlapping(aStart, aEnd, bStart, bEnd)).isFalse();
    }

    @Test
    @DisplayName("formatDuration() 格式化正確")
    void shouldFormatDuration() {
        assertThat(DateTimeUtils.formatDuration(3661)).isEqualTo("01:01:01");
        assertThat(DateTimeUtils.formatDuration(0)).isEqualTo("00:00:00");
    }

    @Test
    @DisplayName("parseIso() 解析正確")
    void shouldParseIsoString() {
        LocalDateTime result = DateTimeUtils.parseIso("2026-03-06T10:00:00");
        assertThat(result).isEqualTo(BASE);
    }

    @Test
    @DisplayName("parseIso() 無效字串回傳 null")
    void shouldReturnNullOnInvalidString() {
        assertThat(DateTimeUtils.parseIso("NOT_A_DATE")).isNull();
        assertThat(DateTimeUtils.parseIso(null)).isNull();
        assertThat(DateTimeUtils.parseIso("")).isNull();
    }
}

package com.gsrm.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * 日期時間工具類別.
 *
 * <p>提供常用的日期時間格式化、解析、計算工具方法。
 * 所有方法均為靜態方法，此類別不可被實例化。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
public final class DateTimeUtils {

    /** 標準 ISO 格式：{@code yyyy-MM-dd'T'HH:mm:ss}. */
    public static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /** 顯示用格式：{@code yyyy-MM-dd HH:mm:ss}. */
    public static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 檔案名稱用格式：{@code yyyyMMdd_HHmmss}. */
    public static final DateTimeFormatter FILE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    /** 台北時區. */
    public static final ZoneId TAIPEI_ZONE = ZoneId.of("Asia/Taipei");

    /**
     * 私有建構子，防止實例化.
     */
    private DateTimeUtils() {
        throw new UnsupportedOperationException("工具類別不可實例化");
    }

    /**
     * 將 LocalDateTime 格式化為 ISO 字串.
     *
     * @param dateTime 日期時間
     * @return 格式化字串，若 dateTime 為 null 則回傳空字串
     */
    public static String toIsoString(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(ISO_FORMATTER);
    }

    /**
     * 將 LocalDateTime 格式化為顯示字串.
     *
     * @param dateTime 日期時間
     * @return 格式化字串，若 dateTime 為 null 則回傳 "-"
     */
    public static String toDisplayString(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(DISPLAY_FORMATTER);
    }

    /**
     * 將 LocalDateTime 格式化為檔案名稱安全的字串.
     *
     * @param dateTime 日期時間
     * @return 格式化字串，若 dateTime 為 null 則使用當前時間
     */
    public static String toFilenameString(LocalDateTime dateTime) {
        LocalDateTime dt = (dateTime != null) ? dateTime : LocalDateTime.now();
        return dt.format(FILE_FORMATTER);
    }

    /**
     * 解析 ISO 格式字串為 LocalDateTime.
     *
     * @param dateTimeStr 日期時間字串
     * @return 解析結果，解析失敗時回傳 null
     */
    public static LocalDateTime parseIso(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) return null;
        try {
            return LocalDateTime.parse(dateTimeStr, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            // 嘗試標準 ISO 格式
            try {
                return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    /**
     * 計算兩個時間之間的秒數差（絕對值）.
     *
     * @param start 開始時間
     * @param end   結束時間
     * @return 秒數差，若任一參數為 null 則回傳 0
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return Math.abs(ChronoUnit.SECONDS.between(start, end));
    }

    /**
     * 計算兩個時間之間的分鐘數差（絕對值）.
     *
     * @param start 開始時間
     * @param end   結束時間
     * @return 分鐘數差，若任一參數為 null 則回傳 0
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return Math.abs(ChronoUnit.MINUTES.between(start, end));
    }

    /**
     * 檢查兩個時間區間是否重疊.
     *
     * <p>採用「重疊 = A.start &lt; B.end AND A.end &gt; B.start」判斷。</p>
     *
     * @param aStart 第一個區間的開始時間
     * @param aEnd   第一個區間的結束時間
     * @param bStart 第二個區間的開始時間
     * @param bEnd   第二個區間的結束時間
     * @return 如果重疊則回傳 true
     */
    public static boolean isOverlapping(LocalDateTime aStart, LocalDateTime aEnd,
                                         LocalDateTime bStart, LocalDateTime bEnd) {
        if (aStart == null || aEnd == null || bStart == null || bEnd == null) return false;
        return aStart.isBefore(bEnd) && aEnd.isAfter(bStart);
    }

    /**
     * 將秒數格式化為「HH:mm:ss」字串.
     *
     * @param totalSeconds 總秒數
     * @return 格式化字串，如 "01:23:45"
     */
    public static String formatDuration(long totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;
        long hours   = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * 取得當前台北時間.
     *
     * @return 台北時區的當前時間
     */
    public static LocalDateTime nowTaipei() {
        return LocalDateTime.now(TAIPEI_ZONE);
    }
}

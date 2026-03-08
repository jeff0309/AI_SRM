package com.gsrm.util;

import com.gsrm.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.regex.Pattern;

/**
 * 輸入驗證工具類別.
 *
 * <p>提供常用的欄位驗證靜態方法，驗證失敗時統一拋出 {@link ValidationException}。
 * 此類別不可被實例化。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
public final class ValidationUtils {

    /** 電子郵件正則表達式. */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * 私有建構子，防止實例化.
     */
    private ValidationUtils() {
        throw new UnsupportedOperationException("工具類別不可實例化");
    }

    /* ─────────── Null / 空值檢查 ─────────── */

    /**
     * 驗證物件不為 null.
     *
     * @param value     要驗證的值
     * @param fieldName 欄位名稱（用於錯誤訊息）
     * @throws ValidationException 如果值為 null
     */
    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " 不可為空");
        }
    }

    /**
     * 驗證字串不為空白.
     *
     * @param value     要驗證的字串
     * @param fieldName 欄位名稱
     * @throws ValidationException 如果字串為 null 或空白
     */
    public static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " 不可為空白");
        }
    }

    /**
     * 驗證集合不為空.
     *
     * @param collection 要驗證的集合
     * @param fieldName  欄位名稱
     * @throws ValidationException 如果集合為 null 或空
     */
    public static void requireNonEmpty(Collection<?> collection, String fieldName) {
        if (collection == null || collection.isEmpty()) {
            throw new ValidationException(fieldName + " 不可為空");
        }
    }

    /* ─────────── 範圍檢查 ─────────── */

    /**
     * 驗證整數值在指定範圍內.
     *
     * @param value     要驗證的值
     * @param min       最小值（含）
     * @param max       最大值（含）
     * @param fieldName 欄位名稱
     * @throws ValidationException 如果值超出範圍
     */
    public static void requireInRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(fieldName, value,
                    "須介於 " + min + " 到 " + max + " 之間");
        }
    }

    /**
     * 驗證 Double 值在指定範圍內.
     *
     * @param value     要驗證的值
     * @param min       最小值（含）
     * @param max       最大值（含）
     * @param fieldName 欄位名稱
     * @throws ValidationException 如果值超出範圍
     */
    public static void requireInRange(double value, double min, double max, String fieldName) {
        if (value < min || value > max) {
            throw new ValidationException(fieldName, value,
                    "須介於 " + min + " 到 " + max + " 之間");
        }
    }

    /* ─────────── 時間驗證 ─────────── */

    /**
     * 驗證結束時間必須晚於開始時間.
     *
     * @param start 開始時間
     * @param end   結束時間
     * @param label 時間範圍描述（用於錯誤訊息）
     * @throws ValidationException 如果時間關係不正確
     */
    public static void requireValidTimeRange(LocalDateTime start, LocalDateTime end, String label) {
        requireNonNull(start, label + " 開始時間");
        requireNonNull(end, label + " 結束時間");
        if (!end.isAfter(start)) {
            throw new ValidationException(label + " 結束時間必須晚於開始時間");
        }
    }

    /**
     * 驗證時間不早於現在.
     *
     * @param dateTime  要驗證的時間
     * @param fieldName 欄位名稱
     * @throws ValidationException 如果時間早於現在
     */
    public static void requireFutureOrPresent(LocalDateTime dateTime, String fieldName) {
        requireNonNull(dateTime, fieldName);
        if (dateTime.isBefore(LocalDateTime.now())) {
            throw new ValidationException(fieldName + " 不可早於現在時間");
        }
    }

    /* ─────────── 字串長度 ─────────── */

    /**
     * 驗證字串長度不超過上限.
     *
     * @param value     要驗證的字串
     * @param maxLength 最大長度
     * @param fieldName 欄位名稱
     * @throws ValidationException 如果超出長度限制
     */
    public static void requireMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationException(fieldName, value.length(),
                    "長度不可超過 " + maxLength + " 字元");
        }
    }

    /* ─────────── 格式驗證 ─────────── */

    /**
     * 驗證電子郵件格式.
     *
     * @param email     電子郵件字串
     * @param fieldName 欄位名稱
     * @throws ValidationException 如果格式不正確
     */
    public static void requireValidEmail(String email, String fieldName) {
        if (email != null && !email.isBlank() && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException(fieldName, email, "電子郵件格式不正確");
        }
    }

    /**
     * 驗證經度值合法（-180 到 180）.
     *
     * @param longitude 經度值
     * @throws ValidationException 如果不合法
     */
    public static void requireValidLongitude(double longitude) {
        requireInRange(longitude, -180.0, 180.0, "經度");
    }

    /**
     * 驗證緯度值合法（-90 到 90）.
     *
     * @param latitude 緯度值
     * @throws ValidationException 如果不合法
     */
    public static void requireValidLatitude(double latitude) {
        requireInRange(latitude, -90.0, 90.0, "緯度");
    }
}

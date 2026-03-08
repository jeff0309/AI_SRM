package com.gsrm.domain.dto.request;

import com.gsrm.domain.enums.FrequencyBand;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 建立衛星請求 DTO.
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatelliteCreateRequest {

    /**
     * 衛星名稱.
     */
    @NotBlank(message = "名稱不可為空")
    @Size(max = 100, message = "名稱不可超過 100 字元")
    private String name;

    /**
     * 衛星代碼.
     */
    @Size(max = 20, message = "代碼不可超過 20 字元")
    private String code;

    /**
     * 所屬公司.
     */
    @Size(max = 100, message = "公司名稱不可超過 100 字元")
    private String company;

    /**
     * 頻段.
     */
    @NotNull(message = "頻段不可為空")
    private FrequencyBand frequencyBand;

    /**
     * 每日最低 Pass 數.
     */
    @NotNull(message = "每日最低 Pass 數不可為空")
    @Min(value = 0, message = "每日最低 Pass 數不可為負數")
    private Integer minDailyPasses;

    /**
     * 最小有效 Pass 秒數.
     */
    @NotNull(message = "最小有效 Pass 秒數不可為空")
    @Min(value = 1, message = "最小有效 Pass 秒數須大於 0")
    private Integer minPassDuration;

    /**
     * 優先權權重 (1-100).
     */
    @NotNull(message = "優先權權重不可為空")
    @Min(value = 1, message = "優先權權重須介於 1-100")
    @Max(value = 100, message = "優先權權重須介於 1-100")
    private Integer priorityWeight;

    /**
     * 是否為緊急任務衛星.
     */
    private Boolean isEmergency;

    /**
     * 描述.
     */
    @Size(max = 500, message = "描述不可超過 500 字元")
    private String description;

    /**
     * 聯絡人.
     */
    @Size(max = 100, message = "聯絡人不可超過 100 字元")
    private String contactPerson;

    /**
     * 聯絡電子郵件.
     */
    @Email(message = "電子郵件格式不正確")
    @Size(max = 100, message = "電子郵件不可超過 100 字元")
    private String contactEmail;

    /**
     * 是否啟用.
     */
    private Boolean enabled;

    /**
     * 地面站偏好列表.
     */
    private List<GroundStationPreferenceDto> groundStationPreferences;

    /**
     * 地面站偏好 DTO.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroundStationPreferenceDto {
        
        /**
         * 地面站 ID.
         */
        @NotNull(message = "地面站 ID 不可為空")
        private Long groundStationId;
        
        /**
         * 偏好順序.
         */
        @NotNull(message = "偏好順序不可為空")
        @Min(value = 1, message = "偏好順序須大於 0")
        private Integer preferenceOrder;
        
        /**
         * 是否為必要地面站.
         */
        private Boolean isMandatory;
    }
}

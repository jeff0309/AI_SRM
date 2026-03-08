package com.gsrm.domain.dto.request;

import com.gsrm.domain.enums.FrequencyBand;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地面站請求 DTO.
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroundStationRequest {

    /**
     * 地面站名稱.
     */
    @NotBlank(message = "名稱不可為空")
    @Size(max = 100, message = "名稱不可超過 100 字元")
    private String name;

    /**
     * 地面站代碼.
     */
    @Size(max = 20, message = "代碼不可超過 20 字元")
    private String code;

    /**
     * 經度.
     */
    @NotNull(message = "經度不可為空")
    @DecimalMin(value = "-180.0", message = "經度須介於 -180 到 180 之間")
    @DecimalMax(value = "180.0", message = "經度須介於 -180 到 180 之間")
    private Double longitude;

    /**
     * 緯度.
     */
    @NotNull(message = "緯度不可為空")
    @DecimalMin(value = "-90.0", message = "緯度須介於 -90 到 90 之間")
    @DecimalMax(value = "90.0", message = "緯度須介於 -90 到 90 之間")
    private Double latitude;

    /**
     * 高度（公尺）.
     */
    @DecimalMin(value = "0.0", message = "高度不可為負數")
    private Double altitude;

    /**
     * 前置準備時間（秒）.
     */
    @NotNull(message = "前置準備時間不可為空")
    @Min(value = 0, message = "前置準備時間不可為負數")
    private Integer setupTime;

    /**
     * 回復時間（秒）.
     */
    @NotNull(message = "回復時間不可為空")
    @Min(value = 0, message = "回復時間不可為負數")
    private Integer teardownTime;

    /**
     * 支援頻段.
     */
    @NotNull(message = "頻段不可為空")
    private FrequencyBand frequencyBand;

    /**
     * 最小仰角（度）.
     */
    @DecimalMin(value = "0.0", message = "最小仰角須介於 0 到 90 之間")
    @DecimalMax(value = "90.0", message = "最小仰角須介於 0 到 90 之間")
    private Double minElevation;

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
     * 聯絡電話.
     */
    @Size(max = 50, message = "聯絡電話不可超過 50 字元")
    private String contactPhone;

    /**
     * 是否啟用.
     */
    private Boolean enabled;
}

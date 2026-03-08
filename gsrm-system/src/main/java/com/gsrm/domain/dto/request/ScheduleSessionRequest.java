package com.gsrm.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 排程 Session 請求 DTO.
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleSessionRequest {

    /**
     * Session 名稱.
     */
    @NotBlank(message = "名稱不可為空")
    @Size(max = 100, message = "名稱不可超過 100 字元")
    private String name;

    /**
     * 描述.
     */
    @Size(max = 500, message = "描述不可超過 500 字元")
    private String description;

    /**
     * 排程開始時間.
     */
    @NotNull(message = "排程開始時間不可為空")
    private LocalDateTime scheduleStartTime;

    /**
     * 排程結束時間.
     */
    @NotNull(message = "排程結束時間不可為空")
    private LocalDateTime scheduleEndTime;

    /**
     * 關聯的衛星 ID 集合（可為空，後續可再設定）.
     */
    private Set<Long> satelliteIds;

    /**
     * 關聯的地面站 ID 集合（可為空，後續可再設定）.
     */
    private Set<Long> groundStationIds;

    /**
     * 使用的縮短策略名稱.
     */
    @Size(max = 50, message = "策略名稱不可超過 50 字元")
    private String shorteningStrategy;
}

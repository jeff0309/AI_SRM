package com.gsrm.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 時間槽模型.
 * 
 * <p>代表一個時間區間，用於排程引擎內部計算。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {

    /**
     * 開始時間.
     */
    private LocalDateTime startTime;

    /**
     * 結束時間.
     */
    private LocalDateTime endTime;

    /**
     * 計算持續時間（秒）.
     * 
     * @return 持續時間秒數
     */
    public long getDurationSeconds() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return Duration.between(startTime, endTime).getSeconds();
    }

    /**
     * 檢查是否與另一個時間槽重疊.
     * 
     * @param other 另一個時間槽
     * @return 如果重疊則回傳 true
     */
    public boolean overlaps(TimeSlot other) {
        if (other == null || startTime == null || endTime == null ||
            other.startTime == null || other.endTime == null) {
            return false;
        }
        return startTime.isBefore(other.endTime) && endTime.isAfter(other.startTime);
    }

    /**
     * 檢查是否與另一個時間槽重疊（考慮間隔）.
     * 
     * @param other 另一個時間槽
     * @param gapSeconds 間隔秒數
     * @return 如果重疊則回傳 true
     */
    public boolean overlapsWithGap(TimeSlot other, int gapSeconds) {
        if (other == null || startTime == null || endTime == null ||
            other.startTime == null || other.endTime == null) {
            return false;
        }
        return startTime.isBefore(other.endTime.plusSeconds(gapSeconds)) && 
               endTime.plusSeconds(gapSeconds).isAfter(other.startTime);
    }

    /**
     * 計算與另一個時間槽的重疊時間（秒）.
     * 
     * @param other 另一個時間槽
     * @return 重疊的秒數，如果不重疊則回傳 0
     */
    public long getOverlapSeconds(TimeSlot other) {
        if (!overlaps(other)) {
            return 0;
        }
        LocalDateTime overlapStart = startTime.isAfter(other.startTime) ? startTime : other.startTime;
        LocalDateTime overlapEnd = endTime.isBefore(other.endTime) ? endTime : other.endTime;
        return Duration.between(overlapStart, overlapEnd).getSeconds();
    }

    /**
     * 檢查指定時間是否在此時間槽內.
     * 
     * @param time 要檢查的時間
     * @return 如果在時間槽內則回傳 true
     */
    public boolean contains(LocalDateTime time) {
        if (time == null || startTime == null || endTime == null) {
            return false;
        }
        return !time.isBefore(startTime) && !time.isAfter(endTime);
    }

    /**
     * 建立縮短後的時間槽（從開始縮短）.
     * 
     * @param newStartTime 新的開始時間
     * @return 新的時間槽
     */
    public TimeSlot shortenFromStart(LocalDateTime newStartTime) {
        return TimeSlot.builder()
                .startTime(newStartTime)
                .endTime(endTime)
                .build();
    }

    /**
     * 建立縮短後的時間槽（從結束縮短）.
     * 
     * @param newEndTime 新的結束時間
     * @return 新的時間槽
     */
    public TimeSlot shortenFromEnd(LocalDateTime newEndTime) {
        return TimeSlot.builder()
                .startTime(startTime)
                .endTime(newEndTime)
                .build();
    }
}

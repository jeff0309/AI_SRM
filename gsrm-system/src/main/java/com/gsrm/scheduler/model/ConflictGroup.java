package com.gsrm.scheduler.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 衝突群組模型.
 * 
 * <p>代表一組在同一地面站時間上互相衝突的 Pass 候選。
 * 排程引擎使用此模型來處理衝突解決。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConflictGroup {

    /**
     * 地面站 ID.
     */
    private Long groundStationId;

    /**
     * 地面站名稱.
     */
    private String groundStationName;

    /**
     * 衝突的 Pass 候選列表.
     */
    @Builder.Default
    private List<PassCandidate> candidates = new ArrayList<>();

    /**
     * 新增候選到衝突群組.
     * 
     * @param candidate Pass 候選
     */
    public void addCandidate(PassCandidate candidate) {
        if (candidates == null) {
            candidates = new ArrayList<>();
        }
        candidates.add(candidate);
    }

    /**
     * 取得依優先權排序的候選列表.
     * 
     * @return 排序後的候選列表（優先權高的在前）
     */
    public List<PassCandidate> getCandidatesByPriority() {
        if (candidates == null) {
            return new ArrayList<>();
        }
        return candidates.stream()
                .sorted((a, b) -> b.comparePriority(a))
                .collect(Collectors.toList());
    }

    /**
     * 取得依 AOS 時間排序的候選列表.
     * 
     * @return 排序後的候選列表（早的在前）
     */
    public List<PassCandidate> getCandidatesByAos() {
        if (candidates == null) {
            return new ArrayList<>();
        }
        return candidates.stream()
                .sorted(Comparator.comparing(PassCandidate::getOriginalAos))
                .collect(Collectors.toList());
    }

    /**
     * 取得候選數量.
     * 
     * @return 候選數量
     */
    public int size() {
        return candidates != null ? candidates.size() : 0;
    }

    /**
     * 檢查是否為空.
     * 
     * @return 如果沒有候選則回傳 true
     */
    public boolean isEmpty() {
        return candidates == null || candidates.isEmpty();
    }

    /**
     * 檢查是否有衝突（超過一個候選）.
     * 
     * @return 如果有衝突則回傳 true
     */
    public boolean hasConflict() {
        return size() > 1;
    }

    /**
     * 取得緊急任務候選.
     * 
     * @return 緊急任務候選列表
     */
    public List<PassCandidate> getEmergencyCandidates() {
        if (candidates == null) {
            return new ArrayList<>();
        }
        return candidates.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsEmergency()))
                .collect(Collectors.toList());
    }

    /**
     * 計算總衝突時間（秒）.
     * 
     * @return 衝突時間總秒數
     */
    public long getTotalConflictSeconds() {
        if (candidates == null || candidates.size() < 2) {
            return 0;
        }
        
        long totalConflict = 0;
        List<PassCandidate> sortedCandidates = getCandidatesByAos();
        
        for (int i = 0; i < sortedCandidates.size() - 1; i++) {
            TimeSlot current = sortedCandidates.get(i).getScheduledTimeSlot();
            for (int j = i + 1; j < sortedCandidates.size(); j++) {
                TimeSlot next = sortedCandidates.get(j).getScheduledTimeSlot();
                totalConflict += current.getOverlapSeconds(next);
            }
        }
        
        return totalConflict;
    }
}

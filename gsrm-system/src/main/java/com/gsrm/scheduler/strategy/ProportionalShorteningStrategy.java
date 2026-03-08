package com.gsrm.scheduler.strategy;

import com.gsrm.scheduler.model.ConflictGroup;
import com.gsrm.scheduler.model.PassCandidate;
import com.gsrm.scheduler.model.TimeSlot;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 等比例縮短策略.
 * 
 * <p>當 Pass 發生衝突時，嘗試依據衝突時間比例縮短雙方的 Pass，
 * 使兩者都能排入。如果縮短後不滿足最小持續時間，則依優先權決定拒絕。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Component("proportionalShorteningStrategy")
public class ProportionalShorteningStrategy implements PassShorteningStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStrategyName() {
        return "PROPORTIONAL";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "等比例縮短策略：依據衝突時間比例縮短雙方的 Pass，使兩者盡可能都能排入";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PassCandidate> resolveConflict(ConflictGroup conflictGroup) {
        List<PassCandidate> result = new ArrayList<>();
        
        if (conflictGroup == null || conflictGroup.isEmpty()) {
            return result;
        }
        
        // 如果沒有衝突，全部排入
        if (!conflictGroup.hasConflict()) {
            conflictGroup.getCandidates().forEach(PassCandidate::markAsScheduled);
            return conflictGroup.getCandidates();
        }
        
        // 依 AOS 時間排序
        List<PassCandidate> sorted = conflictGroup.getCandidatesByAos();
        
        // 先將所有候選標記為待排程
        for (PassCandidate candidate : sorted) {
            candidate.setScheduledAos(candidate.getOriginalAos());
            candidate.setScheduledLos(candidate.getOriginalLos());
        }
        
        // 處理每對衝突
        for (int i = 0; i < sorted.size(); i++) {
            PassCandidate current = sorted.get(i);
            
            // 如果已被拒絕，跳過
            if (!current.getIsAllowed() && current.getRejectionReason() != null) {
                continue;
            }
            
            for (int j = i + 1; j < sorted.size(); j++) {
                PassCandidate next = sorted.get(j);
                
                // 如果已被拒絕，跳過
                if (!next.getIsAllowed() && next.getRejectionReason() != null) {
                    continue;
                }
                
                // 檢查是否衝突
                if (current.conflictsWith(next)) {
                    resolveConflictPair(current, next);
                }
            }
        }
        
        // 標記最終狀態
        for (PassCandidate candidate : sorted) {
            if (candidate.getRejectionReason() == null) {
                if (candidate.getShortenedSeconds() != null && candidate.getShortenedSeconds() > 0) {
                    candidate.markAsShortened(candidate.getScheduledAos(), candidate.getScheduledLos());
                } else {
                    candidate.markAsScheduled();
                }
            }
            result.add(candidate);
        }
        
        return result;
    }

    /**
     * 解決兩個衝突的 Pass.
     * 
     * @param earlier 較早的 Pass
     * @param later 較晚的 Pass
     */
    private void resolveConflictPair(PassCandidate earlier, PassCandidate later) {
        TimeSlot earlierSlot = earlier.getScheduledTimeSlot();
        TimeSlot laterSlot = later.getScheduledTimeSlot();
        
        int gap = earlier.getStationGap() != null ? earlier.getStationGap() : 0;
        
        // 計算衝突時間
        LocalDateTime conflictStart = laterSlot.getStartTime();
        LocalDateTime conflictEnd = earlierSlot.getEndTime().plusSeconds(gap);
        
        if (!conflictStart.isBefore(conflictEnd)) {
            return; // 沒有衝突
        }
        
        long overlapSeconds = java.time.Duration.between(conflictStart, conflictEnd).getSeconds();
        
        // 等比例分配縮短時間
        long earlierDuration = earlierSlot.getDurationSeconds();
        long laterDuration = laterSlot.getDurationSeconds();
        long totalDuration = earlierDuration + laterDuration;
        
        long earlierShorten = (long) Math.ceil(overlapSeconds * earlierDuration / (double) totalDuration);
        long laterShorten = overlapSeconds - earlierShorten;
        
        // 檢查縮短後是否滿足最小持續時間
        int earlierMinDuration = earlier.getMinPassDuration() != null ? earlier.getMinPassDuration() : 60;
        int laterMinDuration = later.getMinPassDuration() != null ? later.getMinPassDuration() : 60;
        
        long earlierNewDuration = earlierDuration - earlierShorten;
        long laterNewDuration = laterDuration - laterShorten;
        
        // 如果雙方都能滿足最小持續時間
        if (earlierNewDuration >= earlierMinDuration && laterNewDuration >= laterMinDuration) {
            // 縮短較早的 Pass 的結束時間
            LocalDateTime newEarlierLos = earlierSlot.getEndTime().minusSeconds(earlierShorten);
            earlier.setScheduledLos(newEarlierLos);
            earlier.setShortenedSeconds((int) earlierShorten);
            
            // 縮短較晚的 Pass 的開始時間
            LocalDateTime newLaterAos = laterSlot.getStartTime().plusSeconds(laterShorten);
            later.setScheduledAos(newLaterAos);
            later.setShortenedSeconds((int) laterShorten);
            
        } else {
            // 依優先權決定誰被拒絕
            if (earlier.comparePriority(later) >= 0) {
                // 較早的 Pass 優先權較高，拒絕較晚的
                later.markAsRejected(
                    "與 " + earlier.getSatelliteName() + " 的 Pass 衝突，縮短後不滿足最小持續時間",
                    earlier.getRequestId()
                );
            } else {
                // 較晚的 Pass 優先權較高，拒絕較早的
                earlier.markAsRejected(
                    "與 " + later.getSatelliteName() + " 的 Pass 衝突，縮短後不滿足最小持續時間",
                    later.getRequestId()
                );
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean attemptShorten(PassCandidate pass, PassCandidate conflictingPass, int minDuration) {
        TimeSlot passSlot = pass.getScheduledTimeSlot();
        TimeSlot conflictSlot = conflictingPass.getScheduledTimeSlot();
        
        int gap = pass.getStationGap() != null ? pass.getStationGap() : 0;
        
        // 判斷誰在前
        if (passSlot.getStartTime().isBefore(conflictSlot.getStartTime())) {
            // pass 在前，縮短 pass 的結束時間
            LocalDateTime newLos = conflictSlot.getStartTime().minusSeconds(gap);
            long newDuration = java.time.Duration.between(passSlot.getStartTime(), newLos).getSeconds();
            
            if (newDuration >= minDuration) {
                pass.markAsShortened(passSlot.getStartTime(), newLos);
                return true;
            }
        } else {
            // pass 在後，縮短 pass 的開始時間
            LocalDateTime newAos = conflictSlot.getEndTime().plusSeconds(gap);
            long newDuration = java.time.Duration.between(newAos, passSlot.getEndTime()).getSeconds();
            
            if (newDuration >= minDuration) {
                pass.markAsShortened(newAos, passSlot.getEndTime());
                return true;
            }
        }
        
        return false;
    }
}

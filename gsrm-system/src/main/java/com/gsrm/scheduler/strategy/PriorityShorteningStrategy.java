package com.gsrm.scheduler.strategy;

import com.gsrm.scheduler.model.ConflictGroup;
import com.gsrm.scheduler.model.PassCandidate;
import com.gsrm.scheduler.model.TimeSlot;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 依優先權縮短策略.
 * 
 * <p>當 Pass 發生衝突時，優先保留高優先權衛星的時間，
 * 嘗試縮短低優先權衛星的 Pass。如果縮短後不滿足最小持續時間，則拒絕低優先權的 Pass。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Component("priorityShorteningStrategy")
public class PriorityShorteningStrategy implements PassShorteningStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStrategyName() {
        return "PRIORITY";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "依優先權縮短策略：優先保留高優先權衛星的時間，縮短或拒絕低優先權的 Pass";
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
        
        // 依優先權排序（高優先權在前）
        List<PassCandidate> sorted = conflictGroup.getCandidatesByPriority();
        
        // 已排入的 Pass 列表
        List<PassCandidate> scheduled = new ArrayList<>();
        
        for (PassCandidate candidate : sorted) {
            boolean canSchedule = true;
            PassCandidate conflictingPass = null;
            
            // 檢查是否與已排入的 Pass 衝突
            for (PassCandidate scheduledPass : scheduled) {
                if (candidate.conflictsWith(scheduledPass)) {
                    canSchedule = false;
                    conflictingPass = scheduledPass;
                    break;
                }
            }
            
            if (canSchedule) {
                candidate.markAsScheduled();
                scheduled.add(candidate);
            } else {
                // 嘗試縮短當前候選（因為它優先權較低）
                int minDuration = candidate.getMinPassDuration() != null ? 
                        candidate.getMinPassDuration() : 60;
                
                if (attemptShorten(candidate, conflictingPass, minDuration)) {
                    // 縮短後再次檢查是否還有衝突
                    boolean stillConflicts = false;
                    for (PassCandidate sp : scheduled) {
                        if (candidate.conflictsWith(sp)) {
                            stillConflicts = true;
                            break;
                        }
                    }
                    
                    if (!stillConflicts) {
                        scheduled.add(candidate);
                    } else {
                        candidate.markAsRejected(
                            "與高優先權 Pass 衝突，縮短後仍有衝突",
                            conflictingPass.getRequestId()
                        );
                    }
                } else {
                    candidate.markAsRejected(
                        "與 " + conflictingPass.getSatelliteName() + " 的高優先權 Pass 衝突",
                        conflictingPass.getRequestId()
                    );
                }
            }
            
            result.add(candidate);
        }
        
        return result;
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
        LocalDateTime passStart = passSlot.getStartTime();
        LocalDateTime passEnd = passSlot.getEndTime();
        LocalDateTime conflictStart = conflictSlot.getStartTime();
        LocalDateTime conflictEnd = conflictSlot.getEndTime();
        
        if (passStart.isBefore(conflictStart)) {
            // pass 在前，需要提前結束
            LocalDateTime newLos = conflictStart.minusSeconds(gap);
            
            if (newLos.isAfter(passStart)) {
                long newDuration = java.time.Duration.between(passStart, newLos).getSeconds();
                
                if (newDuration >= minDuration) {
                    pass.markAsShortened(passStart, newLos);
                    return true;
                }
            }
        } else {
            // pass 在後，需要延後開始
            LocalDateTime newAos = conflictEnd.plusSeconds(gap);
            
            if (newAos.isBefore(passEnd)) {
                long newDuration = java.time.Duration.between(newAos, passEnd).getSeconds();
                
                if (newDuration >= minDuration) {
                    pass.markAsShortened(newAos, passEnd);
                    return true;
                }
            }
        }
        
        return false;
    }
}

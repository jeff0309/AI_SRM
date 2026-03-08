package com.gsrm.scheduler.strategy;

import com.gsrm.scheduler.model.ConflictGroup;
import com.gsrm.scheduler.model.PassCandidate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 不縮短策略.
 * 
 * <p>當 Pass 發生衝突時，直接依據優先權選擇一個排入，其餘拒絕。
 * 此策略不允許任何 Pass 時間縮短。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Component("noShorteningStrategy")
public class NoShorteningStrategy implements PassShorteningStrategy {

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStrategyName() {
        return "NO_SHORTENING";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return "不縮短策略：依優先權選擇一個 Pass 排入，其餘直接拒絕";
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
        
        // 依優先權排序
        List<PassCandidate> sorted = conflictGroup.getCandidatesByPriority();
        
        // 已排入的 Pass 列表
        List<PassCandidate> scheduled = new ArrayList<>();
        
        for (PassCandidate candidate : sorted) {
            boolean hasConflict = false;
            
            // 檢查是否與已排入的 Pass 衝突
            for (PassCandidate scheduledPass : scheduled) {
                if (candidate.conflictsWith(scheduledPass)) {
                    hasConflict = true;
                    candidate.markAsRejected(
                        "與 " + scheduledPass.getSatelliteName() + " 的 Pass 衝突",
                        scheduledPass.getRequestId()
                    );
                    break;
                }
            }
            
            if (!hasConflict) {
                candidate.markAsScheduled();
                scheduled.add(candidate);
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
        // 此策略不允許縮短
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean allowsShortening() {
        return false;
    }
}

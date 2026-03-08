package com.gsrm.scheduler.strategy;

import com.gsrm.scheduler.model.PassCandidate;
import com.gsrm.scheduler.model.ConflictGroup;

import java.util.List;

/**
 * Pass 縮短策略介面.
 * 
 * <p>使用策略模式 (Strategy Pattern) 定義 Pass 衝突時的縮短邏輯。
 * 不同的策略實作可以動態切換，以適應不同的排程需求。</p>
 * 
 * <p>策略包括：</p>
 * <ul>
 *   <li>不縮短 - 直接拒絕衝突的 Pass</li>
 *   <li>等比例縮短 - 依據時間比例縮短雙方</li>
 *   <li>依優先權縮短 - 優先保留高優先權衛星的時間</li>
 * </ul>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
public interface PassShorteningStrategy {

    /**
     * 取得策略名稱.
     * 
     * @return 策略名稱
     */
    String getStrategyName();

    /**
     * 取得策略描述.
     * 
     * @return 策略描述
     */
    String getDescription();

    /**
     * 解決衝突群組.
     * 
     * <p>處理一組在時間上衝突的 Pass 候選，決定哪些 Pass 可以排入、
     * 哪些需要縮短、哪些需要拒絕。</p>
     * 
     * @param conflictGroup 衝突群組
     * @return 處理後的 Pass 候選列表
     */
    List<PassCandidate> resolveConflict(ConflictGroup conflictGroup);

    /**
     * 嘗試縮短單一 Pass.
     * 
     * <p>嘗試將 Pass 縮短到可以排入的狀態。</p>
     * 
     * @param pass 要縮短的 Pass
     * @param conflictingPass 衝突的 Pass
     * @param minDuration 此衛星允許的最小持續時間（秒）
     * @return 如果成功縮短則回傳 true
     */
    boolean attemptShorten(PassCandidate pass, PassCandidate conflictingPass, int minDuration);

    /**
     * 檢查是否允許縮短.
     * 
     * @return 如果此策略允許縮短 Pass 則回傳 true
     */
    default boolean allowsShortening() {
        return true;
    }
}

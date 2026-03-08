package com.gsrm.domain.enums;

/**
 * 衛星 Pass 狀態列舉.
 * 
 * <p>定義衛星通過（Pass）在排程系統中的各種狀態。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
public enum PassStatus {
    
    /**
     * 待處理狀態.
     * Pass 需求已匯入但尚未進行排程處理。
     */
    PENDING("待處理", "Pass request imported but not yet scheduled"),
    
    /**
     * 已排入狀態.
     * Pass 已成功排入排程中。
     */
    SCHEDULED("已排入", "Pass successfully scheduled"),
    
    /**
     * 已縮短狀態.
     * Pass 因衝突而被縮短時間，但仍滿足最小操作時間。
     */
    SHORTENED("已縮短", "Pass shortened due to conflict but meets minimum duration"),
    
    /**
     * 已拒絕狀態.
     * Pass 因衝突或不符合條件而被拒絕排入。
     */
    REJECTED("已拒絕", "Pass rejected due to conflict or constraint violation"),
    
    /**
     * 已取消狀態.
     * Pass 被手動取消或因維護時間而取消。
     */
    CANCELLED("已取消", "Pass cancelled manually or due to maintenance"),
    
    /**
     * 強制排入狀態.
     * Pass 由管理員手動強制排入。
     */
    FORCED("強制排入", "Pass manually forced into schedule");
    
    private final String displayName;
    private final String description;
    
    /**
     * 建構 Pass 狀態列舉值.
     * 
     * @param displayName 顯示名稱
     * @param description 狀態描述
     */
    PassStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * 取得顯示名稱.
     * 
     * @return 中文顯示名稱
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 取得狀態描述.
     * 
     * @return 英文描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 檢查此狀態是否為成功排入的狀態.
     * 
     * @return 如果是成功狀態則回傳 true
     */
    public boolean isSuccessful() {
        return this == SCHEDULED || this == SHORTENED || this == FORCED;
    }
    
    /**
     * 檢查此狀態是否為失敗狀態.
     * 
     * @return 如果是失敗狀態則回傳 true
     */
    public boolean isFailed() {
        return this == REJECTED || this == CANCELLED;
    }
}

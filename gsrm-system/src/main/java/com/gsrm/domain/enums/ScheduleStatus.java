package com.gsrm.domain.enums;

/**
 * 排程 Session 狀態列舉.
 * 
 * <p>定義排程 Session 在生命週期中的各種狀態。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
public enum ScheduleStatus {
    
    /**
     * 草稿狀態.
     * Session 已建立但尚未開始排程。
     */
    DRAFT("草稿", "Session created but scheduling not started"),
    
    /**
     * 排程中狀態.
     * 排程引擎正在處理此 Session。
     */
    PROCESSING("排程中", "Scheduling engine is processing this session"),
    
    /**
     * 已完成狀態.
     * 排程已完成，可進行檢視與匯出。
     */
    COMPLETED("已完成", "Scheduling completed, ready for review and export"),
    
    /**
     * 已發布狀態.
     * 排程結果已匯出並確認發布。
     */
    PUBLISHED("已發布", "Schedule results exported and confirmed"),
    
    /**
     * 已作廢狀態.
     * Session 已被作廢，不再有效。
     */
    CANCELLED("已作廢", "Session has been invalidated");
    
    private final String displayName;
    private final String description;
    
    /**
     * 建構排程狀態列舉值.
     * 
     * @param displayName 顯示名稱
     * @param description 狀態描述
     */
    ScheduleStatus(String displayName, String description) {
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
     * 檢查是否可以執行排程.
     * 
     * @return 如果可以執行排程則回傳 true
     */
    public boolean canSchedule() {
        return this == DRAFT;
    }
    
    /**
     * 檢查是否可以編輯.
     * 
     * @return 如果可以編輯則回傳 true
     */
    public boolean canEdit() {
        return this == DRAFT || this == COMPLETED;
    }
    
    /**
     * 檢查是否可以重置.
     * 
     * @return 如果可以重置則回傳 true
     */
    public boolean canReset() {
        return this == COMPLETED || this == PROCESSING;
    }
}

package com.gsrm.domain.enums;

/**
 * 使用者角色列舉.
 * 
 * <p>定義系統中的使用者權限角色。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
public enum UserRole {
    
    /**
     * 系統管理員.
     * 擁有所有權限，包括使用者管理。
     */
    ADMIN("系統管理員", "Full system access including user management"),
    
    /**
     * 排程操作員.
     * 可執行排程、匯入匯出、手動調整等操作。
     */
    OPERATOR("排程操作員", "Can perform scheduling operations, import/export, manual adjustments"),
    
    /**
     * 檢視者.
     * 只能檢視排程結果與歷史資料，無法進行修改。
     */
    VIEWER("檢視者", "Read-only access to schedules and history");
    
    private final String displayName;
    private final String description;
    
    /**
     * 建構使用者角色列舉值.
     * 
     * @param displayName 顯示名稱
     * @param description 角色描述
     */
    UserRole(String displayName, String description) {
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
     * 取得角色描述.
     * 
     * @return 英文描述
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 檢查是否具有管理權限.
     * 
     * @return 如果具有管理權限則回傳 true
     */
    public boolean hasAdminAccess() {
        return this == ADMIN;
    }
    
    /**
     * 檢查是否具有操作權限.
     * 
     * @return 如果具有操作權限則回傳 true
     */
    public boolean hasOperatorAccess() {
        return this == ADMIN || this == OPERATOR;
    }
    
    /**
     * 取得 Spring Security 角色名稱.
     * 
     * @return 帶 ROLE_ 前綴的角色名稱
     */
    public String getAuthority() {
        return "ROLE_" + this.name();
    }
}

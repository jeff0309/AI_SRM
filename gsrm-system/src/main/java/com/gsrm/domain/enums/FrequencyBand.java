package com.gsrm.domain.enums;

/**
 * 地面站支援的頻段類型列舉.
 * 
 * <p>定義衛星通訊使用的頻段：</p>
 * <ul>
 *   <li>X - X 頻段 (8-12 GHz)</li>
 *   <li>S - S 頻段 (2-4 GHz)</li>
 *   <li>XS - 同時支援 X 與 S 頻段</li>
 * </ul>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
public enum FrequencyBand {
    
    /**
     * X 頻段 (8-12 GHz).
     * 主要用於軍事衛星通訊和深空探測。
     */
    X("X-Band", "8-12 GHz"),
    
    /**
     * S 頻段 (2-4 GHz).
     * 主要用於氣象衛星和部分通訊衛星。
     */
    S("S-Band", "2-4 GHz"),
    
    /**
     * 同時支援 X 與 S 頻段.
     * 具備雙頻段能力的地面站。
     */
    XS("X/S-Band", "2-12 GHz");
    
    private final String displayName;
    private final String frequencyRange;
    
    /**
     * 建構頻段列舉值.
     * 
     * @param displayName 顯示名稱
     * @param frequencyRange 頻率範圍
     */
    FrequencyBand(String displayName, String frequencyRange) {
        this.displayName = displayName;
        this.frequencyRange = frequencyRange;
    }
    
    /**
     * 取得顯示名稱.
     * 
     * @return 顯示名稱
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * 取得頻率範圍.
     * 
     * @return 頻率範圍描述
     */
    public String getFrequencyRange() {
        return frequencyRange;
    }
    
    /**
     * 檢查此頻段是否與目標頻段相容.
     * 
     * <p>XS 頻段與 X 和 S 頻段都相容。</p>
     * 
     * @param target 目標頻段
     * @return 如果相容則回傳 true
     */
    public boolean isCompatibleWith(FrequencyBand target) {
        if (this == XS || target == XS) {
            return true;
        }
        return this == target;
    }
}

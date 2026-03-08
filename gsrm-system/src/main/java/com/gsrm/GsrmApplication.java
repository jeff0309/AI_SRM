package com.gsrm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ground Station Resource Management System (GSRM) 主程式入口.
 * 
 * <p>此系統供衛星地面站營運商使用，處理每週排程循環並支援緊急任務調度。
 * 系統規模預計管理 30 個以上的地面站（支援 X, S, XS 頻段）與超過 30 顆衛星的資源分配。</p>
 * 
 * <p>主要功能包含：</p>
 * <ul>
 *   <li>地面站 (Ground Station) CRUD 管理</li>
 *   <li>衛星 (Satellite) CRUD 管理</li>
 *   <li>衛星需求匯入（XML/CSV 格式）</li>
 *   <li>排程 Session 管理與執行</li>
 *   <li>衝突處理與策略模式切換</li>
 *   <li>甘特圖視覺化與結果匯出</li>
 * </ul>
 * 
 * @author Jeff
 * @since 2026-03-06
 * @version 1.0.0
 */
@SpringBootApplication
public class GsrmApplication {

    /**
     * 應用程式主入口方法.
     * 
     * @param args 命令列參數
     */
    public static void main(String[] args) {
        SpringApplication.run(GsrmApplication.class, args);
    }
}

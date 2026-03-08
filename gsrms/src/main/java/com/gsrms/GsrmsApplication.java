package com.gsrms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Ground Station Resource Management System (GSRMS) 主應用程式入口點。
 * 
 * <p>此系統為衛星地面站營運商提供完整的資源管理功能，包含：
 * <ul>
 *   <li>地面站與衛星資源的 CRUD 管理</li>
 *   <li>衛星通訊需求 (Pass) 的排程處理</li>
 *   <li>衝突偵測與解決機制</li>
 *   <li>甘特圖視覺化呈現</li>
 *   <li>多格式資料匯入匯出</li>
 * </ul>
 * </p>
 * 
 * @author Jeff
 * @version 1.0.0
 * @since 2026-03-06
 */
@SpringBootApplication
@EnableJpaAuditing
public class GsrmsApplication {

    /**
     * 應用程式主進入點。
     * 
     * @param args 命令列參數
     */
    public static void main(String[] args) {
        SpringApplication.run(GsrmsApplication.class, args);
    }
}

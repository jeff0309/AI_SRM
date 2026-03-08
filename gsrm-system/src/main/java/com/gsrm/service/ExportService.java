package com.gsrm.service;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 匯出服務介面.
 *
 * <p>定義將排程結果匯出為不同格式（XML / CSV）的業務邏輯。
 * 透過策略模式動態選擇匯出器。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
public interface ExportService {

    /**
     * 將排程 Session 的結果匯出至 HTTP 回應（供下載）.
     *
     * <p>依據 format 參數（"XML" 或 "CSV"）自動選擇對應的匯出器，
     * 並將輸出串流直接寫入 {@link HttpServletResponse}。</p>
     *
     * @param sessionId 排程 Session ID
     * @param format    匯出格式（"XML" / "CSV"，不分大小寫）
     * @param response  HTTP 回應（用於設定 Content-Type 與觸發下載）
     */
    void exportScheduleResult(Long sessionId, String format, HttpServletResponse response);

    /**
     * 取得所有支援的匯出格式.
     *
     * @return 格式列表（如 ["CSV", "XML"]）
     */
    java.util.List<String> getSupportedFormats();
}

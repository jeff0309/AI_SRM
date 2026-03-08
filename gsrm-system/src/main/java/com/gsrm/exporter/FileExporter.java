package com.gsrm.exporter;

import com.gsrm.domain.entity.ScheduledPass;

import java.io.OutputStream;
import java.util.List;

/**
 * 檔案匯出器介面.
 *
 * <p>使用策略模式 (Strategy Pattern) 定義排程結果的匯出操作。
 * 不同實作對應不同輸出格式（XML、CSV）。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
public interface FileExporter {

    /**
     * 取得匯出格式名稱.
     *
     * @return 格式名稱，例如 "XML"、"CSV"
     */
    String getSupportedFormat();

    /**
     * 取得輸出檔案的副檔名.
     *
     * @return 副檔名（不含點號），例如 "xml"、"csv"
     */
    String getFileExtension();

    /**
     * 取得 Content-Type（MIME Type）.
     *
     * @return MIME Type 字串
     */
    String getContentType();

    /**
     * 將排程結果匯出至輸出串流.
     *
     * @param passes       要匯出的 Pass 列表
     * @param sessionName  所屬 Session 名稱（寫入標頭用）
     * @param outputStream 目標輸出串流
     * @throws ExportException 匯出失敗時拋出
     */
    void export(List<ScheduledPass> passes, String sessionName, OutputStream outputStream)
            throws ExportException;

    /**
     * 匯出例外.
     */
    class ExportException extends Exception {

        /**
         * 建構匯出例外.
         *
         * @param message 錯誤訊息
         */
        public ExportException(String message) {
            super(message);
        }

        /**
         * 建構匯出例外（含原因）.
         *
         * @param message 錯誤訊息
         * @param cause   原因
         */
        public ExportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

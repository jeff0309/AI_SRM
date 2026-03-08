package com.gsrm.importer;

import com.gsrm.domain.entity.SatelliteRequest;
import com.gsrm.domain.entity.StationUnavailability;

import java.io.InputStream;
import java.util.List;

/**
 * 檔案匯入器介面.
 * 
 * <p>使用策略模式 (Strategy Pattern) 定義檔案匯入的抽象操作。
 * 不同的實作支援不同的檔案格式（XML、CSV、TXT）。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
public interface FileImporter {

    /**
     * 取得支援的檔案類型.
     * 
     * @return 檔案類型描述
     */
    String getSupportedFileType();

    /**
     * 取得支援的副檔名列表.
     * 
     * @return 副檔名列表（不含點號）
     */
    List<String> getSupportedExtensions();

    /**
     * 檢查是否支援指定的檔案.
     * 
     * @param filename 檔案名稱
     * @return 如果支援則回傳 true
     */
    default boolean supports(String filename) {
        if (filename == null) {
            return false;
        }
        String lowerFilename = filename.toLowerCase();
        return getSupportedExtensions().stream()
                .anyMatch(ext -> lowerFilename.endsWith("." + ext));
    }

    /**
     * 匯入衛星需求.
     * 
     * @param inputStream 輸入串流
     * @param sessionId 目標 Session ID
     * @return 匯入的需求列表
     * @throws ImportException 匯入失敗時拋出
     */
    List<SatelliteRequest> importRequests(InputStream inputStream, Long sessionId) throws ImportException;

    /**
     * 匯入地面站維護時段.
     * 
     * @param inputStream 輸入串流
     * @return 匯入的維護時段列表
     * @throws ImportException 匯入失敗時拋出
     */
    default List<StationUnavailability> importUnavailabilities(InputStream inputStream) throws ImportException {
        throw new UnsupportedOperationException("此匯入器不支援維護時段匯入");
    }

    /**
     * 匯入例外.
     */
    class ImportException extends Exception {
        
        /**
         * 建構匯入例外.
         * 
         * @param message 錯誤訊息
         */
        public ImportException(String message) {
            super(message);
        }

        /**
         * 建構匯入例外.
         * 
         * @param message 錯誤訊息
         * @param cause 原因
         */
        public ImportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

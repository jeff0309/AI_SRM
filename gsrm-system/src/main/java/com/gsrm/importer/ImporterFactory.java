package com.gsrm.importer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 匯入器工廠.
 * 
 * <p>使用工廠模式 (Factory Pattern) 根據檔案類型取得對應的匯入器。</p>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Component
public class ImporterFactory {

    /**
     * 匯入器映射表.
     */
    private final Map<String, FileImporter> importerMap;

    /**
     * 建構匯入器工廠.
     * 
     * <p>透過 Spring 依賴注入自動收集所有匯入器實作。</p>
     * 
     * @param importers 所有匯入器列表
     */
    @Autowired
    public ImporterFactory(List<FileImporter> importers) {
        this.importerMap = new HashMap<>();
        for (FileImporter importer : importers) {
            importerMap.put(importer.getSupportedFileType().toUpperCase(), importer);
        }
    }

    /**
     * 根據檔案名稱取得對應的匯入器.
     * 
     * @param filename 檔案名稱
     * @return 匯入器
     * @throws IllegalArgumentException 如果找不到支援的匯入器
     */
    public FileImporter getImporter(String filename) {
        for (FileImporter importer : importerMap.values()) {
            if (importer.supports(filename)) {
                return importer;
            }
        }
        throw new IllegalArgumentException("找不到支援此檔案格式的匯入器: " + filename);
    }

    /**
     * 根據檔案類型取得匯入器.
     * 
     * @param fileType 檔案類型（如 "CSV", "XML"）
     * @return 匯入器
     * @throws IllegalArgumentException 如果找不到對應的匯入器
     */
    public FileImporter getImporterByType(String fileType) {
        FileImporter importer = importerMap.get(fileType.toUpperCase());
        if (importer == null) {
            throw new IllegalArgumentException("找不到此類型的匯入器: " + fileType);
        }
        return importer;
    }

    /**
     * 取得所有支援的檔案類型.
     * 
     * @return 檔案類型列表
     */
    public List<String> getSupportedFileTypes() {
        return importerMap.keySet().stream().sorted().toList();
    }

    /**
     * 檢查是否支援指定的檔案.
     * 
     * @param filename 檔案名稱
     * @return 如果支援則回傳 true
     */
    public boolean isSupported(String filename) {
        return importerMap.values().stream().anyMatch(i -> i.supports(filename));
    }
}

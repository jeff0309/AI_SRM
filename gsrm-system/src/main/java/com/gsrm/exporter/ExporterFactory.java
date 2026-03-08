package com.gsrm.exporter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 匯出器工廠.
 *
 * <p>使用工廠模式 (Factory Pattern) 根據格式名稱取得對應的匯出器實作。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Component
public class ExporterFactory {

    private final Map<String, FileExporter> exporterMap;

    /**
     * 建構匯出器工廠，透過 Spring 自動注入所有 FileExporter 實作.
     *
     * @param exporters 所有 FileExporter 實作列表
     */
    @Autowired
    public ExporterFactory(List<FileExporter> exporters) {
        this.exporterMap = new HashMap<>();
        for (FileExporter exporter : exporters) {
            exporterMap.put(exporter.getSupportedFormat().toUpperCase(), exporter);
        }
    }

    /**
     * 根據格式名稱取得匯出器.
     *
     * @param format 格式名稱，不分大小寫（如 "XML"、"CSV"）
     * @return 對應的 FileExporter
     * @throws IllegalArgumentException 格式不支援時拋出
     */
    public FileExporter getExporter(String format) {
        FileExporter exporter = exporterMap.get(format.toUpperCase());
        if (exporter == null) {
            throw new IllegalArgumentException(
                    "不支援的匯出格式: " + format + "，可用格式: " + getSupportedFormats());
        }
        return exporter;
    }

    /**
     * 取得所有支援的格式名稱列表.
     *
     * @return 格式名稱列表（已排序）
     */
    public List<String> getSupportedFormats() {
        return exporterMap.keySet().stream().sorted().toList();
    }

    /**
     * 判斷是否支援指定格式.
     *
     * @param format 格式名稱
     * @return 支援則回傳 true
     */
    public boolean isSupported(String format) {
        return format != null && exporterMap.containsKey(format.toUpperCase());
    }
}

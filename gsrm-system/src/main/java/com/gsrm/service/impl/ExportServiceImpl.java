package com.gsrm.service.impl;

import com.gsrm.domain.entity.ScheduledPass;
import com.gsrm.domain.entity.ScheduleSession;
import com.gsrm.exception.ResourceNotFoundException;
import com.gsrm.exception.ValidationException;
import com.gsrm.exporter.ExporterFactory;
import com.gsrm.exporter.FileExporter;
import com.gsrm.repository.ScheduledPassRepository;
import com.gsrm.service.ExportService;
import com.gsrm.service.ScheduleService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

/**
 * 匯出服務實作.
 *
 * <p>依 format 參數透過 {@link ExporterFactory} 選擇對應策略進行排程結果匯出，
 * 並將輸出直接寫入 HTTP 回應串流。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExportServiceImpl implements ExportService {

    private final ExporterFactory         exporterFactory;
    private final ScheduleService         scheduleService;
    private final ScheduledPassRepository passRepository;

    /** {@inheritDoc} */
    @Override
    public void exportScheduleResult(Long sessionId, String format, HttpServletResponse response) {
        if (!exporterFactory.isSupported(format)) {
            throw new ValidationException(
                    "不支援的匯出格式：" + format + "，可用格式：" + exporterFactory.getSupportedFormats());
        }

        ScheduleSession session = scheduleService.getSessionById(sessionId);
        List<ScheduledPass> passes = passRepository.findAllowedBySessionOrderByAos(sessionId);

        FileExporter exporter = exporterFactory.getExporter(format);

        // 設定 HTTP 回應標頭
        String filename = "schedule_" + session.getName().replaceAll("[^a-zA-Z0-9_-]", "_")
                          + "." + exporter.getFileExtension();
        response.setContentType(exporter.getContentType());
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        response.setCharacterEncoding("UTF-8");

        log.info("[ExportService] 開始匯出，Session：{}，格式：{}，Pass 數：{}",
                session.getName(), format, passes.size());

        try {
            exporter.export(passes, session.getName(), response.getOutputStream());
            log.info("[ExportService] 匯出完成：{}", filename);
        } catch (FileExporter.ExportException e) {
            throw new ValidationException("排程結果匯出失敗：" + e.getMessage(), e);
        } catch (IOException e) {
            throw new ValidationException("無法寫入輸出串流：" + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getSupportedFormats() {
        return exporterFactory.getSupportedFormats();
    }
}

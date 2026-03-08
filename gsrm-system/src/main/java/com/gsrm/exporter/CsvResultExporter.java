package com.gsrm.exporter;

import com.gsrm.domain.entity.ScheduledPass;
import com.opencsv.CSVWriter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * CSV 排程結果匯出器.
 *
 * <p>將排程結果匯出為 CSV 格式，Header 欄位：</p>
 * <pre>
 * SatelliteName, GroundStation, FrequencyBand, AOS, LOS, ScheduledAOS, ScheduledLOS,
 * Status, IsAllowed, ShortenedSeconds, RejectionReason
 * </pre>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Component
public class CsvResultExporter implements FileExporter {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final String[] HEADER = {
        "SatelliteName", "GroundStation", "FrequencyBand",
        "OriginalAOS", "OriginalLOS",
        "ScheduledAOS", "ScheduledLOS",
        "Status", "IsAllowed", "ShortenedSeconds", "RejectionReason"
    };

    /** {@inheritDoc} */
    @Override
    public String getSupportedFormat() { return "CSV"; }

    /** {@inheritDoc} */
    @Override
    public String getFileExtension() { return "csv"; }

    /** {@inheritDoc} */
    @Override
    public String getContentType() { return "text/csv; charset=UTF-8"; }

    /** {@inheritDoc} */
    @Override
    public void export(List<ScheduledPass> passes, String sessionName, OutputStream outputStream)
            throws ExportException {
        try (CSVWriter writer = new CSVWriter(
                new OutputStreamWriter(outputStream, java.nio.charset.StandardCharsets.UTF_8))) {

            // BOM for Excel UTF-8 compatibility
            outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            writer.writeNext(HEADER);

            for (ScheduledPass p : passes) {
                writer.writeNext(toRow(p));
            }

            writer.flush();

        } catch (IOException e) {
            throw new ExportException("匯出 CSV 失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 將 ScheduledPass 轉換為 CSV 資料列.
     *
     * @param p ScheduledPass 實體
     * @return CSV 欄位陣列
     */
    private String[] toRow(ScheduledPass p) {
        return new String[]{
            p.getSatellite()     != null ? p.getSatellite().getName()      : "",
            p.getGroundStation() != null ? p.getGroundStation().getName()  : "",
            p.getFrequencyBand() != null ? p.getFrequencyBand().name()     : "",
            p.getOriginalAos()   != null ? p.getOriginalAos().format(DTF)  : "",
            p.getOriginalLos()   != null ? p.getOriginalLos().format(DTF)  : "",
            p.getScheduledAos()  != null ? p.getScheduledAos().format(DTF) : "",
            p.getScheduledLos()  != null ? p.getScheduledLos().format(DTF) : "",
            p.getStatus()        != null ? p.getStatus().name()            : "",
            String.valueOf(Boolean.TRUE.equals(p.getIsAllowed())),
            p.getShortenedSeconds() != null ? String.valueOf(p.getShortenedSeconds()) : "0",
            p.getRejectionReason() != null ? p.getRejectionReason() : ""
        };
    }
}

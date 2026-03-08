package com.gsrm.importer;

import com.gsrm.domain.entity.*;
import com.gsrm.domain.enums.FrequencyBand;
import com.gsrm.domain.enums.PassStatus;
import com.gsrm.repository.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * CSV 需求匯入器.
 * 
 * <p>支援從 CSV 檔案匯入衛星需求。</p>
 * 
 * <p>CSV 格式（含 Header）：</p>
 * <pre>
 * SatelliteName,GroundStationName,FrequencyBand,AOS,LOS
 * FormoSat-7A,Taiwan Hsinchu,X,2026-03-10T08:30:00,2026-03-10T08:45:00
 * </pre>
 * 
 * @author Jeff
 * @since 2026-03-06
 */
@Component
public class CsvRequestImporter implements FileImporter {

    private final SatelliteRepository satelliteRepository;
    private final GroundStationRepository groundStationRepository;
    private final ScheduleSessionRepository sessionRepository;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 建構 CSV 匯入器.
     * 
     * @param satelliteRepository 衛星 Repository
     * @param groundStationRepository 地面站 Repository
     * @param sessionRepository Session Repository
     */
    public CsvRequestImporter(SatelliteRepository satelliteRepository,
                               GroundStationRepository groundStationRepository,
                               ScheduleSessionRepository sessionRepository) {
        this.satelliteRepository = satelliteRepository;
        this.groundStationRepository = groundStationRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSupportedFileType() {
        return "CSV";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList("csv", "txt");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SatelliteRequest> importRequests(InputStream inputStream, Long sessionId) 
            throws ImportException {
        
        List<SatelliteRequest> requests = new ArrayList<>();
        String batchId = UUID.randomUUID().toString().substring(0, 8);
        
        // 取得 Session
        ScheduleSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ImportException("找不到 Session: " + sessionId));

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            List<String[]> lines = reader.readAll();
            
            if (lines.isEmpty()) {
                throw new ImportException("CSV 檔案為空");
            }
            
            // 跳過 Header
            boolean hasHeader = isHeader(lines.get(0));
            int startIndex = hasHeader ? 1 : 0;
            
            for (int i = startIndex; i < lines.size(); i++) {
                String[] line = lines.get(i);
                int lineNumber = i + 1;
                
                try {
                    SatelliteRequest request = parseLine(line, session, batchId, lineNumber);
                    if (request != null) {
                        requests.add(request);
                    }
                } catch (Exception e) {
                    throw new ImportException(
                            String.format("第 %d 行解析錯誤: %s", lineNumber, e.getMessage()), e);
                }
            }
            
        } catch (IOException | CsvException e) {
            throw new ImportException("讀取 CSV 檔案失敗: " + e.getMessage(), e);
        }
        
        return requests;
    }

    /**
     * 檢查是否為 Header 行.
     * 
     * @param line 資料行
     * @return 如果是 Header 則回傳 true
     */
    private boolean isHeader(String[] line) {
        if (line == null || line.length == 0) {
            return false;
        }
        String first = line[0].toLowerCase().trim();
        return first.equals("satellitename") || first.equals("satellite") || 
               first.equals("satellite_name") || first.equals("衛星名稱");
    }

    /**
     * 解析單一資料行.
     * 
     * @param line 資料行
     * @param session 目標 Session
     * @param batchId 批次 ID
     * @param lineNumber 行號
     * @return 衛星需求實體
     * @throws ImportException 解析失敗時拋出
     */
    private SatelliteRequest parseLine(String[] line, ScheduleSession session, 
                                        String batchId, int lineNumber) throws ImportException {
        
        if (line == null || line.length < 5) {
            throw new ImportException("欄位數量不足，需要至少 5 個欄位");
        }
        
        // 跳過空行
        if (Arrays.stream(line).allMatch(s -> s == null || s.trim().isEmpty())) {
            return null;
        }
        
        String satelliteName = line[0].trim();
        String groundStationName = line[1].trim();
        String bandStr = line[2].trim().toUpperCase();
        String aosStr = line[3].trim();
        String losStr = line[4].trim();
        
        // 查詢衛星
        Satellite satellite = satelliteRepository.findByName(satelliteName)
                .orElseThrow(() -> new ImportException("找不到衛星: " + satelliteName));
        
        // 查詢地面站
        GroundStation groundStation = groundStationRepository.findByName(groundStationName)
                .orElseThrow(() -> new ImportException("找不到地面站: " + groundStationName));
        
        // 解析頻段
        FrequencyBand band;
        try {
            band = FrequencyBand.valueOf(bandStr);
        } catch (IllegalArgumentException e) {
            throw new ImportException("無效的頻段: " + bandStr);
        }
        
        // 解析時間
        LocalDateTime aos;
        LocalDateTime los;
        try {
            aos = LocalDateTime.parse(aosStr, DATE_TIME_FORMATTER);
            los = LocalDateTime.parse(losStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ImportException("時間格式錯誤，請使用 ISO 格式 (yyyy-MM-ddTHH:mm:ss)");
        }
        
        // 驗證時間
        if (!los.isAfter(aos)) {
            throw new ImportException("LOS 時間必須在 AOS 時間之後");
        }
        
        return SatelliteRequest.builder()
                .scheduleSession(session)
                .satellite(satellite)
                .groundStation(groundStation)
                .frequencyBand(band)
                .aos(aos)
                .los(los)
                .status(PassStatus.PENDING)
                .importBatchId(batchId)
                .externalRequestId("CSV-" + lineNumber + "-" + batchId)
                .build();
    }
}

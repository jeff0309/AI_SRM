package com.gsrm.importer;

import com.gsrm.domain.entity.GroundStation;
import com.gsrm.domain.entity.SatelliteRequest;
import com.gsrm.domain.entity.StationUnavailability;
import com.gsrm.repository.GroundStationRepository;
import org.springframework.stereotype.Component;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * TXT 地面站維護時段匯入器.
 *
 * <p>支援從純文字檔案匯入地面站不可用時段。</p>
 *
 * <p>格式（每行一筆，以逗號或 Tab 分隔）：</p>
 * <pre>
 * # 地面站名稱,開始時間,結束時間[,原因]
 * Taiwan Hsinchu,2026-03-10T02:00:00,2026-03-10T06:00:00,週期性維護
 * Taiwan Taipei,2026-03-11T01:00:00,2026-03-11T03:00:00,設備校準
 * </pre>
 *
 * <p>以 {@code #} 開頭的行視為注解，略過不解析。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Component
public class TxtUnavailabilityImporter implements FileImporter {

    private final GroundStationRepository groundStationRepository;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 建構 TXT 維護時段匯入器.
     *
     * @param groundStationRepository 地面站 Repository
     */
    public TxtUnavailabilityImporter(GroundStationRepository groundStationRepository) {
        this.groundStationRepository = groundStationRepository;
    }

    /** {@inheritDoc} */
    @Override
    public String getSupportedFileType() {
        return "TXT_UNAVAILABILITY";
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList("txt");
    }

    /**
     * 此匯入器不支援衛星需求匯入，呼叫時拋出 UnsupportedOperationException.
     *
     * {@inheritDoc}
     */
    @Override
    public List<SatelliteRequest> importRequests(InputStream inputStream, Long sessionId)
            throws ImportException {
        throw new UnsupportedOperationException("TxtUnavailabilityImporter 不支援衛星需求匯入");
    }

    /**
     * 從 TXT 輸入串流匯入地面站維護時段.
     *
     * {@inheritDoc}
     */
    @Override
    public List<StationUnavailability> importUnavailabilities(InputStream inputStream)
            throws ImportException {

        List<StationUnavailability> results = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // 略過空行和注解行
                if (line.isEmpty() || line.startsWith("#")) continue;

                try {
                    StationUnavailability unavail = parseLine(line, lineNumber);
                    if (unavail != null) results.add(unavail);
                } catch (ImportException e) {
                    throw new ImportException(
                            String.format("第 %d 行解析錯誤: %s", lineNumber, e.getMessage()), e);
                }
            }

        } catch (IOException e) {
            throw new ImportException("讀取檔案失敗: " + e.getMessage(), e);
        }

        return results;
    }

    /**
     * 解析單一資料行.
     *
     * @param line       原始行字串
     * @param lineNumber 行號（用於錯誤訊息）
     * @return StationUnavailability 實體，空行回傳 null
     * @throws ImportException 解析失敗時拋出
     */
    private StationUnavailability parseLine(String line, int lineNumber) throws ImportException {
        // 支援逗號或 Tab 分隔
        String[] parts = line.contains("\t") ? line.split("\t") : line.split(",");

        if (parts.length < 3) {
            throw new ImportException("欄位不足，需要至少 3 個欄位（地面站名稱,開始時間,結束時間）");
        }

        String stationName = parts[0].trim();
        String startStr    = parts[1].trim();
        String endStr      = parts[2].trim();
        String reason      = parts.length >= 4 ? parts[3].trim() : null;

        if (stationName.isEmpty()) throw new ImportException("地面站名稱不可為空");

        GroundStation station = groundStationRepository.findByName(stationName)
                .orElseThrow(() -> new ImportException("找不到地面站: " + stationName));

        LocalDateTime startTime;
        LocalDateTime endTime;
        try {
            startTime = LocalDateTime.parse(startStr, DTF);
            endTime   = LocalDateTime.parse(endStr,   DTF);
        } catch (DateTimeParseException e) {
            throw new ImportException("時間格式錯誤，請使用 ISO 格式 (yyyy-MM-ddTHH:mm:ss)");
        }

        if (!endTime.isAfter(startTime)) {
            throw new ImportException("結束時間必須晚於開始時間");
        }

        return StationUnavailability.builder()
                .groundStation(station)
                .startTime(startTime)
                .endTime(endTime)
                .reason(reason)
                .maintenanceType("IMPORTED")
                .isRecurring(false)
                .build();
    }
}

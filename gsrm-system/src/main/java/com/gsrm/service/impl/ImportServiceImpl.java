package com.gsrm.service.impl;

import com.gsrm.domain.entity.SatelliteRequest;
import com.gsrm.domain.entity.StationUnavailability;
import com.gsrm.exception.ValidationException;
import com.gsrm.importer.FileImporter;
import com.gsrm.importer.ImporterFactory;
import com.gsrm.repository.SatelliteRequestRepository;
import com.gsrm.service.GroundStationService;
import com.gsrm.service.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 匯入服務實作.
 *
 * <p>依檔案副檔名透過 {@link ImporterFactory} 選擇對應策略進行匯入，
 * 並將結果持久化至資料庫。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ImportServiceImpl implements ImportService {

    private final ImporterFactory            importerFactory;
    private final SatelliteRequestRepository requestRepository;
    private final GroundStationService       groundStationService;

    /* ─────────── 衛星需求匯入 ─────────── */

    /** {@inheritDoc} */
    @Override
    public List<SatelliteRequest> importSatelliteRequests(MultipartFile file,
                                                          Long sessionId,
                                                          Long userId) {
        validateFile(file);

        String filename = file.getOriginalFilename();
        FileImporter importer = importerFactory.getImporter(filename);

        log.info("[ImportService] 開始匯入衛星需求，檔案：{}，Session ID：{}", filename, sessionId);

        List<SatelliteRequest> requests;
        try {
            requests = importer.importRequests(file.getInputStream(), sessionId);
        } catch (FileImporter.ImportException e) {
            throw new ValidationException("衛星需求匯入失敗：" + e.getMessage(), e);
        } catch (IOException e) {
            throw new ValidationException("無法讀取上傳檔案：" + e.getMessage(), e);
        }

        List<SatelliteRequest> saved = requestRepository.saveAll(requests);
        log.info("[ImportService] 衛星需求匯入完成，共 {} 筆", saved.size());
        return saved;
    }

    /* ─────────── 維護時段匯入 ─────────── */

    /** {@inheritDoc} */
    @Override
    public List<StationUnavailability> importStationUnavailabilities(MultipartFile file, Long userId) {
        validateFile(file);

        String filename = file.getOriginalFilename();
        FileImporter importer = importerFactory.getImporter(filename);

        log.info("[ImportService] 開始匯入維護時段，檔案：{}", filename);

        List<StationUnavailability> unavailabilities;
        try {
            unavailabilities = importer.importUnavailabilities(file.getInputStream());
        } catch (FileImporter.ImportException e) {
            throw new ValidationException("維護時段匯入失敗：" + e.getMessage(), e);
        } catch (IOException e) {
            throw new ValidationException("無法讀取上傳檔案：" + e.getMessage(), e);
        }

        int count = groundStationService.importUnavailabilities(unavailabilities, userId);
        log.info("[ImportService] 維護時段匯入完成，共 {} 筆", count);
        return unavailabilities;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getSupportedRequestFormats() {
        return importerFactory.getSupportedFileTypes();
    }

    /* ─────────── 私有輔助 ─────────── */

    /**
     * 驗證上傳的檔案是否有效.
     *
     * @param file 上傳的 MultipartFile
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("上傳檔案不可為空");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new ValidationException("檔案名稱無效");
        }
        if (!importerFactory.isSupported(file.getOriginalFilename())) {
            throw new ValidationException(
                    "不支援的檔案格式：" + file.getOriginalFilename()
                    + "，支援格式：" + importerFactory.getSupportedFileTypes());
        }
    }
}

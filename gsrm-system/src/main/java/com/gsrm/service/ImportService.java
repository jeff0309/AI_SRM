package com.gsrm.service;

import com.gsrm.domain.entity.SatelliteRequest;
import com.gsrm.domain.entity.StationUnavailability;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 匯入服務介面.
 *
 * <p>定義衛星需求檔案與地面站維護時段檔案的匯入業務邏輯。
 * 透過策略模式動態選擇匯入器（XML / CSV / TXT）。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
public interface ImportService {

    /**
     * 匯入衛星需求檔案（XML 或 CSV）.
     *
     * <p>系統會依據檔案副檔名自動選擇對應的匯入器。</p>
     *
     * @param file      上傳的檔案
     * @param sessionId 目標排程 Session ID
     * @param userId    操作者 ID
     * @return 匯入成功的需求列表
     */
    List<SatelliteRequest> importSatelliteRequests(MultipartFile file, Long sessionId, Long userId);

    /**
     * 匯入地面站維護時段檔案（TXT）.
     *
     * @param file   上傳的檔案
     * @param userId 操作者 ID
     * @return 匯入成功的維護時段列表
     */
    List<StationUnavailability> importStationUnavailabilities(MultipartFile file, Long userId);

    /**
     * 取得所有支援的需求檔案匯入格式.
     *
     * @return 格式列表（如 ["CSV", "XML"]）
     */
    List<String> getSupportedRequestFormats();
}

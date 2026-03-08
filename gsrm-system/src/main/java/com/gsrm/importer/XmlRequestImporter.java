package com.gsrm.importer;

import com.gsrm.domain.entity.*;
import com.gsrm.domain.enums.FrequencyBand;
import com.gsrm.domain.enums.PassStatus;
import com.gsrm.repository.*;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * XML 需求匯入器.
 *
 * <p>支援從符合 satellite-request.xsd 格式的 XML 檔案匯入衛星需求。</p>
 *
 * <p>範例 XML 格式：</p>
 * <pre>{@code
 * <SatelliteRequests>
 *   <Header>
 *     <RequestId>REQ-2026-001</RequestId>
 *     <GeneratedAt>2026-03-06T08:00:00</GeneratedAt>
 *     <SchedulePeriodStart>2026-03-10T00:00:00</SchedulePeriodStart>
 *     <SchedulePeriodEnd>2026-03-17T00:00:00</SchedulePeriodEnd>
 *   </Header>
 *   <Requests>
 *     <PassRequest id="PR-001">
 *       <SatelliteName>FormoSat-7A</SatelliteName>
 *       <GroundStationName>Taiwan Hsinchu</GroundStationName>
 *       <FrequencyBand>X</FrequencyBand>
 *       <AOS>2026-03-10T08:30:00</AOS>
 *       <LOS>2026-03-10T08:45:00</LOS>
 *     </PassRequest>
 *   </Requests>
 * </SatelliteRequests>
 * }</pre>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Component
public class XmlRequestImporter implements FileImporter {

    private final SatelliteRepository satelliteRepository;
    private final GroundStationRepository groundStationRepository;
    private final ScheduleSessionRepository sessionRepository;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /**
     * 建構 XML 匯入器.
     *
     * @param satelliteRepository      衛星 Repository
     * @param groundStationRepository  地面站 Repository
     * @param sessionRepository        Session Repository
     */
    public XmlRequestImporter(SatelliteRepository satelliteRepository,
                               GroundStationRepository groundStationRepository,
                               ScheduleSessionRepository sessionRepository) {
        this.satelliteRepository = satelliteRepository;
        this.groundStationRepository = groundStationRepository;
        this.sessionRepository = sessionRepository;
    }

    /** {@inheritDoc} */
    @Override
    public String getSupportedFileType() {
        return "XML";
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getSupportedExtensions() {
        return Arrays.asList("xml");
    }

    /** {@inheritDoc} */
    @Override
    public List<SatelliteRequest> importRequests(InputStream inputStream, Long sessionId)
            throws ImportException {

        List<SatelliteRequest> requests = new ArrayList<>();
        String batchId = UUID.randomUUID().toString().substring(0, 8);

        ScheduleSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ImportException("找不到 Session: " + sessionId));

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 防止 XXE 攻擊
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);
            doc.getDocumentElement().normalize();

            NodeList passRequests = doc.getElementsByTagName("PassRequest");

            for (int i = 0; i < passRequests.getLength(); i++) {
                Element el = (Element) passRequests.item(i);
                String externalId = el.getAttribute("id");

                try {
                    SatelliteRequest req = parseElement(el, session, batchId, externalId);
                    requests.add(req);
                } catch (ImportException e) {
                    throw new ImportException(
                            "解析 PassRequest id='" + externalId + "' 時發生錯誤: " + e.getMessage(), e);
                }
            }

        } catch (ImportException e) {
            throw e;
        } catch (Exception e) {
            throw new ImportException("解析 XML 失敗: " + e.getMessage(), e);
        }

        return requests;
    }

    /**
     * 解析單一 PassRequest 元素.
     *
     * @param el         XML 元素
     * @param session    目標 Session
     * @param batchId    批次 ID
     * @param externalId 外部請求 ID
     * @return SatelliteRequest 實體
     * @throws ImportException 解析失敗時拋出
     */
    private SatelliteRequest parseElement(Element el, ScheduleSession session,
                                           String batchId, String externalId)
            throws ImportException {

        String satelliteName   = getTextContent(el, "SatelliteName");
        String stationName     = getTextContent(el, "GroundStationName");
        String bandStr         = getTextContent(el, "FrequencyBand").toUpperCase();
        String aosStr          = getTextContent(el, "AOS");
        String losStr          = getTextContent(el, "LOS");

        // 選填欄位
        String priorityStr     = getTextContentOrNull(el, "Priority");
        String emergencyStr    = getTextContentOrNull(el, "IsEmergency");
        String notes           = getTextContentOrNull(el, "Notes");

        Satellite satellite = satelliteRepository.findByName(satelliteName)
                .orElseThrow(() -> new ImportException("找不到衛星: " + satelliteName));

        GroundStation station = groundStationRepository.findByName(stationName)
                .orElseThrow(() -> new ImportException("找不到地面站: " + stationName));

        FrequencyBand band;
        try {
            band = FrequencyBand.valueOf(bandStr);
        } catch (IllegalArgumentException e) {
            throw new ImportException("無效的頻段值: " + bandStr);
        }

        LocalDateTime aos;
        LocalDateTime los;
        try {
            aos = LocalDateTime.parse(aosStr, DTF);
            los = LocalDateTime.parse(losStr, DTF);
        } catch (Exception e) {
            throw new ImportException("時間格式錯誤，請使用 ISO 格式 (yyyy-MM-ddTHH:mm:ss)");
        }

        if (!los.isAfter(aos)) {
            throw new ImportException("LOS 必須晚於 AOS");
        }

        int priority   = priorityStr   != null ? Integer.parseInt(priorityStr)  : 5;
        boolean isEmrg = emergencyStr  != null && Boolean.parseBoolean(emergencyStr);

        return SatelliteRequest.builder()
                .scheduleSession(session)
                .satellite(satellite)
                .groundStation(station)
                .frequencyBand(band)
                .aos(aos)
                .los(los)
                .priority(priority)
                .isEmergency(isEmrg)
                .notes(notes)
                .status(PassStatus.PENDING)
                .importBatchId(batchId)
                .externalRequestId(externalId.isBlank() ? "XML-" + batchId : externalId)
                .build();
    }

    /**
     * 取得指定標籤的文字內容（不存在時拋出例外）.
     *
     * @param parent  父元素
     * @param tagName 標籤名稱
     * @return 文字內容
     * @throws ImportException 標籤不存在或為空
     */
    private String getTextContent(Element parent, String tagName) throws ImportException {
        NodeList nl = parent.getElementsByTagName(tagName);
        if (nl.getLength() == 0 || nl.item(0).getTextContent().isBlank()) {
            throw new ImportException("缺少必要欄位: " + tagName);
        }
        return nl.item(0).getTextContent().trim();
    }

    /**
     * 取得指定標籤的文字內容（不存在時回傳 null）.
     *
     * @param parent  父元素
     * @param tagName 標籤名稱
     * @return 文字內容，或 null
     */
    private String getTextContentOrNull(Element parent, String tagName) {
        NodeList nl = parent.getElementsByTagName(tagName);
        if (nl.getLength() == 0) return null;
        String val = nl.item(0).getTextContent().trim();
        return val.isBlank() ? null : val;
    }
}

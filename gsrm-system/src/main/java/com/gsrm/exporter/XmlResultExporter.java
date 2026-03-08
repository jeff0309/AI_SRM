package com.gsrm.exporter;

import com.gsrm.domain.entity.ScheduledPass;
import com.gsrm.domain.enums.PassStatus;
import org.springframework.stereotype.Component;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * XML 排程結果匯出器.
 *
 * <p>依據 schedule-reply.xsd 定義將排程結果匯出為 reply.xml。</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Component
public class XmlResultExporter implements FileExporter {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** {@inheritDoc} */
    @Override
    public String getSupportedFormat() { return "XML"; }

    /** {@inheritDoc} */
    @Override
    public String getFileExtension() { return "xml"; }

    /** {@inheritDoc} */
    @Override
    public String getContentType() { return "application/xml"; }

    /** {@inheritDoc} */
    @Override
    public void export(List<ScheduledPass> passes, String sessionName, OutputStream outputStream)
            throws ExportException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            // 根元素
            Element root = doc.createElement("ScheduleReply");
            root.setAttribute("version", "1.0");
            doc.appendChild(root);

            // Header
            root.appendChild(buildHeader(doc, sessionName));

            // Summary
            root.appendChild(buildSummary(doc, passes));

            // ScheduledPasses
            Element passesEl = doc.createElement("ScheduledPasses");
            passesEl.setAttribute("count", String.valueOf(passes.size()));
            for (ScheduledPass p : passes) {
                passesEl.appendChild(buildPassElement(doc, p));
            }
            root.appendChild(passesEl);

            // 輸出
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(new DOMSource(doc), new StreamResult(outputStream));

        } catch (Exception e) {
            throw new ExportException("匯出 XML 失敗: " + e.getMessage(), e);
        }
    }

    /* ─────────── 私有輔助方法 ─────────── */

    /**
     * 建立 Header 元素.
     *
     * @param doc         Document
     * @param sessionName Session 名稱
     * @return Header Element
     */
    private Element buildHeader(Document doc, String sessionName) {
        Element header = doc.createElement("Header");
        addText(doc, header, "ReplyId",      UUID.randomUUID().toString());
        addText(doc, header, "SessionName",  sessionName);
        addText(doc, header, "GeneratedAt",  LocalDateTime.now().format(DTF));
        return header;
    }

    /**
     * 建立 Summary 元素.
     *
     * @param doc    Document
     * @param passes Pass 列表
     * @return Summary Element
     */
    private Element buildSummary(Document doc, List<ScheduledPass> passes) {
        long total      = passes.size();
        long scheduled  = passes.stream().filter(p -> p.getStatus() == PassStatus.SCHEDULED).count();
        long shortened  = passes.stream().filter(p -> p.getStatus() == PassStatus.SHORTENED).count();
        long rejected   = passes.stream().filter(p -> p.getStatus() == PassStatus.REJECTED).count();
        long forced     = passes.stream().filter(p -> p.getStatus() == PassStatus.FORCED).count();
        long allowed    = passes.stream().filter(p -> Boolean.TRUE.equals(p.getIsAllowed())).count();
        double rate     = total == 0 ? 0.0 : (allowed * 100.0 / total);

        Element sum = doc.createElement("Summary");
        addText(doc, sum, "TotalRequests",      String.valueOf(total));
        addText(doc, sum, "ScheduledCount",     String.valueOf(scheduled));
        addText(doc, sum, "ShortenedCount",     String.valueOf(shortened));
        addText(doc, sum, "RejectedCount",      String.valueOf(rejected));
        addText(doc, sum, "ForcedCount",        String.valueOf(forced));
        addText(doc, sum, "SuccessRate",        String.format("%.2f", rate));
        addText(doc, sum, "ConflictsResolved",
                String.valueOf(shortened + rejected));
        return sum;
    }

    /**
     * 建立單一 Pass 元素.
     *
     * @param doc Document
     * @param p   ScheduledPass 實體
     * @return Pass Element
     */
    private Element buildPassElement(Document doc, ScheduledPass p) {
        Element el = doc.createElement("Pass");
        el.setAttribute("id", String.valueOf(p.getId()));
        if (p.getSatelliteRequest() != null) {
            el.setAttribute("originalRequestId",
                    String.valueOf(p.getSatelliteRequest().getId()));
        }

        addText(doc, el, "SatelliteName",    p.getSatellite().getName());
        addText(doc, el, "GroundStationName",p.getGroundStation().getName());
        addText(doc, el, "FrequencyBand",    p.getFrequencyBand().name());
        addText(doc, el, "OriginalAOS",      fmt(p.getOriginalAos()));
        addText(doc, el, "OriginalLOS",      fmt(p.getOriginalLos()));

        if (p.getScheduledAos() != null) addText(doc, el, "ScheduledAOS", fmt(p.getScheduledAos()));
        if (p.getScheduledLos() != null) addText(doc, el, "ScheduledLOS", fmt(p.getScheduledLos()));

        addText(doc, el, "Status",    p.getStatus().name());
        addText(doc, el, "IsAllowed", String.valueOf(Boolean.TRUE.equals(p.getIsAllowed())));

        if (p.getShortenedSeconds() != null && p.getShortenedSeconds() > 0) {
            addText(doc, el, "ShortenedSeconds", String.valueOf(p.getShortenedSeconds()));
        }
        if (p.getRejectionReason() != null) {
            addText(doc, el, "RejectionReason", p.getRejectionReason());
        }
        if (p.getNotes() != null) {
            addText(doc, el, "Notes", p.getNotes());
        }

        return el;
    }

    /** 快速建立帶文字內容的子元素. */
    private void addText(Document doc, Element parent, String tag, String value) {
        Element child = doc.createElement(tag);
        child.appendChild(doc.createTextNode(value != null ? value : ""));
        parent.appendChild(child);
    }

    /** 格式化 LocalDateTime. */
    private String fmt(LocalDateTime dt) {
        return dt == null ? "" : dt.format(DTF);
    }
}

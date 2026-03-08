package com.gsrms.model.xml;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JAXB root binding class for {@code <ScheduleReply>} —
 * the root element defined in {@code schedule-reply.xsd}.
 *
 * <p>Usage (marshal):</p>
 * <pre>{@code
 * JAXBContext ctx = JAXBContext.newInstance(XmlScheduleReply.class);
 * ctx.createMarshaller().marshal(reply, outputStream);
 * }</pre>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "ScheduleReply", namespace = "http://gsrms.com/xml/reply")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScheduleReplyType", propOrder = {"header", "summary", "scheduledPasses"})
public class XmlScheduleReply {

    @XmlAttribute(name = "version")
    @Builder.Default
    private String version = "1.0";

    @XmlElement(name = "Header",   required = true)
    private ReplyHeader header;

    @XmlElement(name = "Summary",  required = true)
    private Summary summary;

    @XmlElement(name = "ScheduledPasses", required = true)
    private PassList scheduledPasses;

    // ── Inner classes ──

    /**
     * {@code <Header>} element of the schedule reply.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "ReplyHeaderType", propOrder = {
            "replyId", "originalRequestId", "sessionId", "sessionName",
            "generatedAt", "schedulePeriodStart", "schedulePeriodEnd", "generatedBy"
    })
    public static class ReplyHeader {

        @XmlElement(name = "ReplyId",           required = true)
        private String replyId;

        @XmlElement(name = "OriginalRequestId")
        private String originalRequestId;

        @XmlElement(name = "SessionId",         required = true)
        private String sessionId;

        @XmlElement(name = "SessionName",       required = true)
        private String sessionName;

        @XmlElement(name = "GeneratedAt",       required = true)
        private String generatedAt;

        @XmlElement(name = "SchedulePeriodStart", required = true)
        private String schedulePeriodStart;

        @XmlElement(name = "SchedulePeriodEnd",   required = true)
        private String schedulePeriodEnd;

        @XmlElement(name = "GeneratedBy")
        private String generatedBy;
    }

    /**
     * {@code <Summary>} element with scheduling statistics.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "SummaryType", propOrder = {
            "totalRequests", "scheduledCount", "shortenedCount",
            "rejectedCount", "forcedCount", "successRate", "conflictsResolved"
    })
    public static class Summary {

        @XmlElement(name = "TotalRequests",    required = true)
        private int totalRequests;

        @XmlElement(name = "ScheduledCount",   required = true)
        private int scheduledCount;

        @XmlElement(name = "ShortenedCount",   required = true)
        private int shortenedCount;

        @XmlElement(name = "RejectedCount",    required = true)
        private int rejectedCount;

        @XmlElement(name = "ForcedCount",      required = true)
        private int forcedCount;

        @XmlElement(name = "SuccessRate",      required = true)
        private BigDecimal successRate;

        @XmlElement(name = "ConflictsResolved", required = true)
        private int conflictsResolved;
    }

    /**
     * {@code <ScheduledPasses>} wrapper element.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "ScheduledPassListType")
    public static class PassList {

        @XmlAttribute(name = "count")
        private Integer count;

        @XmlElement(name = "Pass")
        @Builder.Default
        private List<XmlScheduledPass> passes = new ArrayList<>();
    }
}

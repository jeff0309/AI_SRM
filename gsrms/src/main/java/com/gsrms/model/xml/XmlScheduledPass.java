package com.gsrms.model.xml;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JAXB binding class for a single {@code <Pass>} element
 * defined in {@code schedule-reply.xsd}.
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScheduledPassType", propOrder = {
        "satelliteName", "groundStationName", "frequencyBand",
        "originalAos", "originalLos", "scheduledAos", "scheduledLos",
        "status", "isAllowed", "durationSeconds", "shortenedSeconds",
        "rejectionReason", "conflictWith", "notes"
})
public class XmlScheduledPass {

    @XmlAttribute(name = "id", required = true)
    private String id;

    @XmlAttribute(name = "originalRequestId")
    private String originalRequestId;

    @XmlElement(name = "SatelliteName",    required = true)
    private String satelliteName;

    @XmlElement(name = "GroundStationName", required = true)
    private String groundStationName;

    @XmlElement(name = "FrequencyBand",    required = true)
    private String frequencyBand;

    @XmlElement(name = "OriginalAOS",      required = true)
    private String originalAos;

    @XmlElement(name = "OriginalLOS",      required = true)
    private String originalLos;

    @XmlElement(name = "ScheduledAOS")
    private String scheduledAos;

    @XmlElement(name = "ScheduledLOS")
    private String scheduledLos;

    @XmlElement(name = "Status",           required = true)
    private String status;

    @XmlElement(name = "IsAllowed",        required = true)
    private Boolean isAllowed;

    @XmlElement(name = "DurationSeconds")
    private Integer durationSeconds;

    @XmlElement(name = "ShortenedSeconds")
    private Integer shortenedSeconds;

    @XmlElement(name = "RejectionReason")
    private String rejectionReason;

    @XmlElement(name = "ConflictWith")
    private String conflictWith;

    @XmlElement(name = "Notes")
    private String notes;
}

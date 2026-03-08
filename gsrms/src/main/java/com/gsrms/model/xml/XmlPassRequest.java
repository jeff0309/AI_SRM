package com.gsrms.model.xml;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JAXB binding class for a single {@code <PassRequest>} element
 * defined in {@code satellite-request.xsd}.
 *
 * <p>Hand-written to avoid jaxb2-maven-plugin / Java 21 incompatibility.</p>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PassRequestType", propOrder = {
        "satelliteName", "groundStationName", "frequencyBand",
        "aos", "los", "maxElevation", "priority", "isEmergency", "notes"
})
public class XmlPassRequest {

    @XmlAttribute(name = "id", required = true)
    private String id;

    @XmlElement(name = "SatelliteName", required = true)
    private String satelliteName;

    @XmlElement(name = "GroundStationName", required = true)
    private String groundStationName;

    @XmlElement(name = "FrequencyBand", required = true)
    private String frequencyBand;

    /** Acquisition of Signal — ISO-8601 string (e.g. 2026-03-17T08:10:00Z) */
    @XmlElement(name = "AOS", required = true)
    private String aos;

    /** Loss of Signal — ISO-8601 string */
    @XmlElement(name = "LOS", required = true)
    private String los;

    @XmlElement(name = "MaxElevation")
    private BigDecimal maxElevation;

    @XmlElement(name = "Priority")
    @Builder.Default
    private Integer priority = 5;

    @XmlElement(name = "IsEmergency")
    @Builder.Default
    private Boolean isEmergency = false;

    @XmlElement(name = "Notes")
    private String notes;
}

package com.gsrms.model.xml;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JAXB binding class for the {@code <Header>} element in
 * {@code satellite-request.xsd}.
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HeaderType", propOrder = {
        "requestId", "generatedAt", "sourceSystem",
        "schedulePeriodStart", "schedulePeriodEnd", "description"
})
public class XmlRequestHeader {

    @XmlElement(name = "RequestId", required = true)
    private String requestId;

    @XmlElement(name = "GeneratedAt", required = true)
    private String generatedAt;

    @XmlElement(name = "SourceSystem")
    private String sourceSystem;

    @XmlElement(name = "SchedulePeriodStart", required = true)
    private String schedulePeriodStart;

    @XmlElement(name = "SchedulePeriodEnd", required = true)
    private String schedulePeriodEnd;

    @XmlElement(name = "Description")
    private String description;
}

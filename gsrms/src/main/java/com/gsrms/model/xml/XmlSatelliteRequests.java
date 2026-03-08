package com.gsrms.model.xml;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * JAXB root binding class for {@code <SatelliteRequests>} —
 * the root element defined in {@code satellite-request.xsd}.
 *
 * <p>Usage (unmarshal):</p>
 * <pre>{@code
 * JAXBContext ctx = JAXBContext.newInstance(XmlSatelliteRequests.class);
 * XmlSatelliteRequests root =
 *     (XmlSatelliteRequests) ctx.createUnmarshaller().unmarshal(inputStream);
 * }</pre>
 *
 * @author Jeff
 * @since 2026-03-06
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "SatelliteRequests", namespace = "http://gsrms.com/xml/request")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SatelliteRequestsType", propOrder = {"header", "requests"})
public class XmlSatelliteRequests {

    @XmlAttribute(name = "version")
    @Builder.Default
    private String version = "1.0";

    @XmlElement(name = "Header", required = true)
    private XmlRequestHeader header;

    @XmlElement(name = "Requests", required = true)
    private RequestList requests;

    // ── Inner list wrapper ──

    /**
     * Wrapper for the {@code <Requests>} element containing
     * zero or more {@code <PassRequest>} children.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "RequestListType")
    public static class RequestList {

        @XmlAttribute(name = "count")
        private Integer count;

        @XmlElement(name = "PassRequest")
        @Builder.Default
        private List<XmlPassRequest> passRequests = new ArrayList<>();
    }
}

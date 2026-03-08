/**
 * Hand-written JAXB binding classes for GSRM XML schemas.
 *
 * <p>These classes replace XJC-generated code because
 * {@code jaxb2-maven-plugin 3.1.0} is incompatible with Java 17+.</p>
 *
 * <ul>
 *   <li>{@link com.gsrms.model.xml.XmlSatelliteRequests} — satellite-request.xsd root</li>
 *   <li>{@link com.gsrms.model.xml.XmlScheduleReply}     — schedule-reply.xsd root</li>
 * </ul>
 */
@XmlSchemaTypes({
        @XmlSchemaType(name = "dateTime", type = String.class)
})
package com.gsrms.model.xml;

import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlSchemaTypes;

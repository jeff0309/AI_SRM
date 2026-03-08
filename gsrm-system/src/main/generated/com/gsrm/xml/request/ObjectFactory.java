//
// 此檔案是由 Eclipse Implementation of JAXB, v3.0.0 所產生 
// 請參閱 https://eclipse-ee4j.github.io/jaxb-ri 
// 一旦重新編譯來源綱要, 對此檔案所做的任何修改都將會遺失. 
// 產生時間: 2026.03.06 於 10:28:11 PM CST 
//


package com.gsrm.xml.request;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.gsrm.xml.request package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _SatelliteRequests_QNAME = new QName("http://gsrm.com/xml/request", "SatelliteRequests");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.gsrm.xml.request
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SatelliteRequestsType }
     * 
     */
    public SatelliteRequestsType createSatelliteRequestsType() {
        return new SatelliteRequestsType();
    }

    /**
     * Create an instance of {@link HeaderType }
     * 
     */
    public HeaderType createHeaderType() {
        return new HeaderType();
    }

    /**
     * Create an instance of {@link RequestListType }
     * 
     */
    public RequestListType createRequestListType() {
        return new RequestListType();
    }

    /**
     * Create an instance of {@link PassRequestType }
     * 
     */
    public PassRequestType createPassRequestType() {
        return new PassRequestType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SatelliteRequestsType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link SatelliteRequestsType }{@code >}
     */
    @XmlElementDecl(namespace = "http://gsrm.com/xml/request", name = "SatelliteRequests")
    public JAXBElement<SatelliteRequestsType> createSatelliteRequests(SatelliteRequestsType value) {
        return new JAXBElement<SatelliteRequestsType>(_SatelliteRequests_QNAME, SatelliteRequestsType.class, null, value);
    }

}

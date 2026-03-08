//
// 此檔案是由 Eclipse Implementation of JAXB, v3.0.0 所產生 
// 請參閱 https://eclipse-ee4j.github.io/jaxb-ri 
// 一旦重新編譯來源綱要, 對此檔案所做的任何修改都將會遺失. 
// 產生時間: 2026.03.06 於 10:28:11 PM CST 
//


package com.gsrm.xml.reply;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.gsrm.xml.reply package. 
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

    private final static QName _ScheduleReply_QNAME = new QName("http://gsrm.com/xml/reply", "ScheduleReply");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.gsrm.xml.reply
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ScheduleReplyType }
     * 
     */
    public ScheduleReplyType createScheduleReplyType() {
        return new ScheduleReplyType();
    }

    /**
     * Create an instance of {@link ReplyHeaderType }
     * 
     */
    public ReplyHeaderType createReplyHeaderType() {
        return new ReplyHeaderType();
    }

    /**
     * Create an instance of {@link SummaryType }
     * 
     */
    public SummaryType createSummaryType() {
        return new SummaryType();
    }

    /**
     * Create an instance of {@link ScheduledPassListType }
     * 
     */
    public ScheduledPassListType createScheduledPassListType() {
        return new ScheduledPassListType();
    }

    /**
     * Create an instance of {@link ScheduledPassType }
     * 
     */
    public ScheduledPassType createScheduledPassType() {
        return new ScheduledPassType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ScheduleReplyType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link ScheduleReplyType }{@code >}
     */
    @XmlElementDecl(namespace = "http://gsrm.com/xml/reply", name = "ScheduleReply")
    public JAXBElement<ScheduleReplyType> createScheduleReply(ScheduleReplyType value) {
        return new JAXBElement<ScheduleReplyType>(_ScheduleReply_QNAME, ScheduleReplyType.class, null, value);
    }

}

//
// 此檔案是由 Eclipse Implementation of JAXB, v3.0.0 所產生 
// 請參閱 https://eclipse-ee4j.github.io/jaxb-ri 
// 一旦重新編譯來源綱要, 對此檔案所做的任何修改都將會遺失. 
// 產生時間: 2026.03.06 於 10:28:11 PM CST 
//


package com.gsrm.xml.reply;

import javax.xml.datatype.XMLGregorianCalendar;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 *                 Header information for the schedule reply.
 *             
 * 
 * <p>ReplyHeaderType complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="ReplyHeaderType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ReplyId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="OriginalRequestId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="SessionId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="SessionName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="GeneratedAt" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="SchedulePeriodStart" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="SchedulePeriodEnd" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="GeneratedBy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReplyHeaderType", propOrder = {
    "replyId",
    "originalRequestId",
    "sessionId",
    "sessionName",
    "generatedAt",
    "schedulePeriodStart",
    "schedulePeriodEnd",
    "generatedBy"
})
public class ReplyHeaderType {

    @XmlElement(name = "ReplyId", required = true)
    protected String replyId;
    @XmlElement(name = "OriginalRequestId")
    protected String originalRequestId;
    @XmlElement(name = "SessionId", required = true)
    protected String sessionId;
    @XmlElement(name = "SessionName", required = true)
    protected String sessionName;
    @XmlElement(name = "GeneratedAt", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar generatedAt;
    @XmlElement(name = "SchedulePeriodStart", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar schedulePeriodStart;
    @XmlElement(name = "SchedulePeriodEnd", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar schedulePeriodEnd;
    @XmlElement(name = "GeneratedBy")
    protected String generatedBy;

    /**
     * 取得 replyId 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReplyId() {
        return replyId;
    }

    /**
     * 設定 replyId 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReplyId(String value) {
        this.replyId = value;
    }

    /**
     * 取得 originalRequestId 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOriginalRequestId() {
        return originalRequestId;
    }

    /**
     * 設定 originalRequestId 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOriginalRequestId(String value) {
        this.originalRequestId = value;
    }

    /**
     * 取得 sessionId 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * 設定 sessionId 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionId(String value) {
        this.sessionId = value;
    }

    /**
     * 取得 sessionName 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionName() {
        return sessionName;
    }

    /**
     * 設定 sessionName 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionName(String value) {
        this.sessionName = value;
    }

    /**
     * 取得 generatedAt 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getGeneratedAt() {
        return generatedAt;
    }

    /**
     * 設定 generatedAt 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setGeneratedAt(XMLGregorianCalendar value) {
        this.generatedAt = value;
    }

    /**
     * 取得 schedulePeriodStart 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSchedulePeriodStart() {
        return schedulePeriodStart;
    }

    /**
     * 設定 schedulePeriodStart 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSchedulePeriodStart(XMLGregorianCalendar value) {
        this.schedulePeriodStart = value;
    }

    /**
     * 取得 schedulePeriodEnd 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getSchedulePeriodEnd() {
        return schedulePeriodEnd;
    }

    /**
     * 設定 schedulePeriodEnd 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setSchedulePeriodEnd(XMLGregorianCalendar value) {
        this.schedulePeriodEnd = value;
    }

    /**
     * 取得 generatedBy 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGeneratedBy() {
        return generatedBy;
    }

    /**
     * 設定 generatedBy 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGeneratedBy(String value) {
        this.generatedBy = value;
    }

}

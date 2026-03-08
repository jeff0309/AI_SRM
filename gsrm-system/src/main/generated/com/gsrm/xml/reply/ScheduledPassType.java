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
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 *                 Individual scheduled pass with result information.
 *             
 * 
 * <p>ScheduledPassType complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="ScheduledPassType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SatelliteName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="GroundStationName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="FrequencyBand" type="{http://gsrm.com/xml/reply}FrequencyBandType"/&gt;
 *         &lt;element name="OriginalAOS" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="OriginalLOS" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="ScheduledAOS" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/&gt;
 *         &lt;element name="ScheduledLOS" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/&gt;
 *         &lt;element name="Status" type="{http://gsrm.com/xml/reply}PassStatusType"/&gt;
 *         &lt;element name="IsAllowed" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="DurationSeconds" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="ShortenedSeconds" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="RejectionReason" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="ConflictWith" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *         &lt;element name="Notes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="originalRequestId" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ScheduledPassType", propOrder = {
    "satelliteName",
    "groundStationName",
    "frequencyBand",
    "originalAOS",
    "originalLOS",
    "scheduledAOS",
    "scheduledLOS",
    "status",
    "isAllowed",
    "durationSeconds",
    "shortenedSeconds",
    "rejectionReason",
    "conflictWith",
    "notes"
})
public class ScheduledPassType {

    @XmlElement(name = "SatelliteName", required = true)
    protected String satelliteName;
    @XmlElement(name = "GroundStationName", required = true)
    protected String groundStationName;
    @XmlElement(name = "FrequencyBand", required = true)
    @XmlSchemaType(name = "string")
    protected FrequencyBandType frequencyBand;
    @XmlElement(name = "OriginalAOS", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar originalAOS;
    @XmlElement(name = "OriginalLOS", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar originalLOS;
    @XmlElement(name = "ScheduledAOS")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar scheduledAOS;
    @XmlElement(name = "ScheduledLOS")
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar scheduledLOS;
    @XmlElement(name = "Status", required = true)
    @XmlSchemaType(name = "string")
    protected PassStatusType status;
    @XmlElement(name = "IsAllowed")
    protected boolean isAllowed;
    @XmlElement(name = "DurationSeconds")
    protected Integer durationSeconds;
    @XmlElement(name = "ShortenedSeconds")
    protected Integer shortenedSeconds;
    @XmlElement(name = "RejectionReason")
    protected String rejectionReason;
    @XmlElement(name = "ConflictWith")
    protected String conflictWith;
    @XmlElement(name = "Notes")
    protected String notes;
    @XmlAttribute(name = "id", required = true)
    protected String id;
    @XmlAttribute(name = "originalRequestId")
    protected String originalRequestId;

    /**
     * 取得 satelliteName 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSatelliteName() {
        return satelliteName;
    }

    /**
     * 設定 satelliteName 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSatelliteName(String value) {
        this.satelliteName = value;
    }

    /**
     * 取得 groundStationName 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGroundStationName() {
        return groundStationName;
    }

    /**
     * 設定 groundStationName 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGroundStationName(String value) {
        this.groundStationName = value;
    }

    /**
     * 取得 frequencyBand 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link FrequencyBandType }
     *     
     */
    public FrequencyBandType getFrequencyBand() {
        return frequencyBand;
    }

    /**
     * 設定 frequencyBand 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link FrequencyBandType }
     *     
     */
    public void setFrequencyBand(FrequencyBandType value) {
        this.frequencyBand = value;
    }

    /**
     * 取得 originalAOS 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getOriginalAOS() {
        return originalAOS;
    }

    /**
     * 設定 originalAOS 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setOriginalAOS(XMLGregorianCalendar value) {
        this.originalAOS = value;
    }

    /**
     * 取得 originalLOS 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getOriginalLOS() {
        return originalLOS;
    }

    /**
     * 設定 originalLOS 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setOriginalLOS(XMLGregorianCalendar value) {
        this.originalLOS = value;
    }

    /**
     * 取得 scheduledAOS 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getScheduledAOS() {
        return scheduledAOS;
    }

    /**
     * 設定 scheduledAOS 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setScheduledAOS(XMLGregorianCalendar value) {
        this.scheduledAOS = value;
    }

    /**
     * 取得 scheduledLOS 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getScheduledLOS() {
        return scheduledLOS;
    }

    /**
     * 設定 scheduledLOS 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setScheduledLOS(XMLGregorianCalendar value) {
        this.scheduledLOS = value;
    }

    /**
     * 取得 status 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link PassStatusType }
     *     
     */
    public PassStatusType getStatus() {
        return status;
    }

    /**
     * 設定 status 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link PassStatusType }
     *     
     */
    public void setStatus(PassStatusType value) {
        this.status = value;
    }

    /**
     * 取得 isAllowed 特性的值.
     * 
     */
    public boolean isIsAllowed() {
        return isAllowed;
    }

    /**
     * 設定 isAllowed 特性的值.
     * 
     */
    public void setIsAllowed(boolean value) {
        this.isAllowed = value;
    }

    /**
     * 取得 durationSeconds 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * 設定 durationSeconds 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setDurationSeconds(Integer value) {
        this.durationSeconds = value;
    }

    /**
     * 取得 shortenedSeconds 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getShortenedSeconds() {
        return shortenedSeconds;
    }

    /**
     * 設定 shortenedSeconds 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setShortenedSeconds(Integer value) {
        this.shortenedSeconds = value;
    }

    /**
     * 取得 rejectionReason 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRejectionReason() {
        return rejectionReason;
    }

    /**
     * 設定 rejectionReason 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRejectionReason(String value) {
        this.rejectionReason = value;
    }

    /**
     * 取得 conflictWith 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getConflictWith() {
        return conflictWith;
    }

    /**
     * 設定 conflictWith 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setConflictWith(String value) {
        this.conflictWith = value;
    }

    /**
     * 取得 notes 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNotes() {
        return notes;
    }

    /**
     * 設定 notes 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNotes(String value) {
        this.notes = value;
    }

    /**
     * 取得 id 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * 設定 id 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
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

}

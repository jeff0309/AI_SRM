//
// 此檔案是由 Eclipse Implementation of JAXB, v3.0.0 所產生 
// 請參閱 https://eclipse-ee4j.github.io/jaxb-ri 
// 一旦重新編譯來源綱要, 對此檔案所做的任何修改都將會遺失. 
// 產生時間: 2026.03.06 於 10:28:11 PM CST 
//


package com.gsrm.xml.request;

import java.math.BigDecimal;
import javax.xml.datatype.XMLGregorianCalendar;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 *                 Individual satellite pass request containing timing and station information.
 *             
 * 
 * <p>PassRequestType complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="PassRequestType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="SatelliteName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="GroundStationName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="FrequencyBand" type="{http://gsrm.com/xml/request}FrequencyBandType"/&gt;
 *         &lt;element name="AOS" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="LOS" type="{http://www.w3.org/2001/XMLSchema}dateTime"/&gt;
 *         &lt;element name="MaxElevation" type="{http://www.w3.org/2001/XMLSchema}decimal" minOccurs="0"/&gt;
 *         &lt;element name="Priority" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/&gt;
 *         &lt;element name="IsEmergency" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/&gt;
 *         &lt;element name="Notes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PassRequestType", propOrder = {
    "satelliteName",
    "groundStationName",
    "frequencyBand",
    "aos",
    "los",
    "maxElevation",
    "priority",
    "isEmergency",
    "notes"
})
public class PassRequestType {

    @XmlElement(name = "SatelliteName", required = true)
    protected String satelliteName;
    @XmlElement(name = "GroundStationName", required = true)
    protected String groundStationName;
    @XmlElement(name = "FrequencyBand", required = true)
    @XmlSchemaType(name = "string")
    protected FrequencyBandType frequencyBand;
    @XmlElement(name = "AOS", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar aos;
    @XmlElement(name = "LOS", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar los;
    @XmlElement(name = "MaxElevation")
    protected BigDecimal maxElevation;
    @XmlElement(name = "Priority", defaultValue = "5")
    protected Integer priority;
    @XmlElement(name = "IsEmergency", defaultValue = "false")
    protected Boolean isEmergency;
    @XmlElement(name = "Notes")
    protected String notes;
    @XmlAttribute(name = "id", required = true)
    protected String id;

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
     * 取得 aos 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getAOS() {
        return aos;
    }

    /**
     * 設定 aos 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setAOS(XMLGregorianCalendar value) {
        this.aos = value;
    }

    /**
     * 取得 los 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getLOS() {
        return los;
    }

    /**
     * 設定 los 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setLOS(XMLGregorianCalendar value) {
        this.los = value;
    }

    /**
     * 取得 maxElevation 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getMaxElevation() {
        return maxElevation;
    }

    /**
     * 設定 maxElevation 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setMaxElevation(BigDecimal value) {
        this.maxElevation = value;
    }

    /**
     * 取得 priority 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * 設定 priority 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPriority(Integer value) {
        this.priority = value;
    }

    /**
     * 取得 isEmergency 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsEmergency() {
        return isEmergency;
    }

    /**
     * 設定 isEmergency 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsEmergency(Boolean value) {
        this.isEmergency = value;
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

}

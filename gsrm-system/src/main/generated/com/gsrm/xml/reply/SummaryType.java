//
// 此檔案是由 Eclipse Implementation of JAXB, v3.0.0 所產生 
// 請參閱 https://eclipse-ee4j.github.io/jaxb-ri 
// 一旦重新編譯來源綱要, 對此檔案所做的任何修改都將會遺失. 
// 產生時間: 2026.03.06 於 10:28:11 PM CST 
//


package com.gsrm.xml.reply;

import java.math.BigDecimal;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 *                 Summary statistics of the scheduling results.
 *             
 * 
 * <p>SummaryType complex type 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * 
 * <pre>
 * &lt;complexType name="SummaryType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="TotalRequests" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="ScheduledCount" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="ShortenedCount" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="RejectedCount" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="ForcedCount" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="SuccessRate" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="ConflictsResolved" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SummaryType", propOrder = {
    "totalRequests",
    "scheduledCount",
    "shortenedCount",
    "rejectedCount",
    "forcedCount",
    "successRate",
    "conflictsResolved"
})
public class SummaryType {

    @XmlElement(name = "TotalRequests")
    protected int totalRequests;
    @XmlElement(name = "ScheduledCount")
    protected int scheduledCount;
    @XmlElement(name = "ShortenedCount")
    protected int shortenedCount;
    @XmlElement(name = "RejectedCount")
    protected int rejectedCount;
    @XmlElement(name = "ForcedCount")
    protected int forcedCount;
    @XmlElement(name = "SuccessRate", required = true)
    protected BigDecimal successRate;
    @XmlElement(name = "ConflictsResolved")
    protected int conflictsResolved;

    /**
     * 取得 totalRequests 特性的值.
     * 
     */
    public int getTotalRequests() {
        return totalRequests;
    }

    /**
     * 設定 totalRequests 特性的值.
     * 
     */
    public void setTotalRequests(int value) {
        this.totalRequests = value;
    }

    /**
     * 取得 scheduledCount 特性的值.
     * 
     */
    public int getScheduledCount() {
        return scheduledCount;
    }

    /**
     * 設定 scheduledCount 特性的值.
     * 
     */
    public void setScheduledCount(int value) {
        this.scheduledCount = value;
    }

    /**
     * 取得 shortenedCount 特性的值.
     * 
     */
    public int getShortenedCount() {
        return shortenedCount;
    }

    /**
     * 設定 shortenedCount 特性的值.
     * 
     */
    public void setShortenedCount(int value) {
        this.shortenedCount = value;
    }

    /**
     * 取得 rejectedCount 特性的值.
     * 
     */
    public int getRejectedCount() {
        return rejectedCount;
    }

    /**
     * 設定 rejectedCount 特性的值.
     * 
     */
    public void setRejectedCount(int value) {
        this.rejectedCount = value;
    }

    /**
     * 取得 forcedCount 特性的值.
     * 
     */
    public int getForcedCount() {
        return forcedCount;
    }

    /**
     * 設定 forcedCount 特性的值.
     * 
     */
    public void setForcedCount(int value) {
        this.forcedCount = value;
    }

    /**
     * 取得 successRate 特性的值.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getSuccessRate() {
        return successRate;
    }

    /**
     * 設定 successRate 特性的值.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setSuccessRate(BigDecimal value) {
        this.successRate = value;
    }

    /**
     * 取得 conflictsResolved 特性的值.
     * 
     */
    public int getConflictsResolved() {
        return conflictsResolved;
    }

    /**
     * 設定 conflictsResolved 特性的值.
     * 
     */
    public void setConflictsResolved(int value) {
        this.conflictsResolved = value;
    }

}

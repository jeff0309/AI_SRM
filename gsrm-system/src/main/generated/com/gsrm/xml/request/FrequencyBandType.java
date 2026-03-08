//
// 此檔案是由 Eclipse Implementation of JAXB, v3.0.0 所產生 
// 請參閱 https://eclipse-ee4j.github.io/jaxb-ri 
// 一旦重新編譯來源綱要, 對此檔案所做的任何修改都將會遺失. 
// 產生時間: 2026.03.06 於 10:28:11 PM CST 
//


package com.gsrm.xml.request;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>FrequencyBandType 的 Java 類別.
 * 
 * <p>下列綱要片段會指定此類別中包含的預期內容.
 * <pre>
 * &lt;simpleType name="FrequencyBandType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="X"/&gt;
 *     &lt;enumeration value="S"/&gt;
 *     &lt;enumeration value="XS"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "FrequencyBandType")
@XmlEnum
public enum FrequencyBandType {

    X,
    S,
    XS;

    public String value() {
        return name();
    }

    public static FrequencyBandType fromValue(String v) {
        return valueOf(v);
    }

}

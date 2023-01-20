/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlPopulationReport {
   @XmlAttribute
   public static final double maxScore = 1.0;
   @XmlAttribute
   public double avgScore;
   @XmlElement
   public int totNumOfXMLElements;
   @XmlElement
   public double avgNumOfXMLElements;
   @XmlElement
   public int totNumOfXMLSimpleElements;
   @XmlElement
   public int totNumOfXMLEmptyElements;
   @XmlElement
   public double avgNumOfXMLEmptyElements;
   @XmlElement
   public double avgRateOfPopulatedElement;
}

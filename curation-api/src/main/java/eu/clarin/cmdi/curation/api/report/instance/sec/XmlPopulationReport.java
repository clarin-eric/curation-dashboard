/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.instance.sec;

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
   public final double maxScore = 1.0;
   @XmlAttribute
   public double score;
   @XmlElement
   public int numOfXMLElements;
   @XmlElement
   public int numOfXMLSimpleElements;
   @XmlElement
   public int numOfXMLEmptyElements;
   @XmlElement
   public double percOfPopulatedElements;
}

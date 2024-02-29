/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.instance.sec;

import jakarta.xml.bind.annotation.*;


/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlPopulationReport {
   @XmlAttribute
   public static final double maxScore = 1.0;
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

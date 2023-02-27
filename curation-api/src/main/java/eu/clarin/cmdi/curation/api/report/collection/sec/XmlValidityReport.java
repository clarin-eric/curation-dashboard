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
public class XmlValidityReport {
   @XmlAttribute
   public double aggregatedScore;
   @XmlAttribute
   public double aggregatedMaxScoreAll;
   @XmlAttribute
   public double aggregatedMaxScoreProcessable;
   @XmlAttribute
   public double scorePercentageAll;
   @XmlAttribute
   public double scorePercentageProcessable;
   @XmlAttribute
   public double avgScoreAll;
   @XmlAttribute
   public double avgScoreProcessable;
   @XmlElement
   public int totNumOfValidRecords;

}

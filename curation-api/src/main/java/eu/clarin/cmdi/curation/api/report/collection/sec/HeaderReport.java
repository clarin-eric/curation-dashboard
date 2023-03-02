/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class HeaderReport {
   @XmlAttribute
   public double aggregatedScore;
   @XmlAttribute
   public double aggregatedMaxScore;
   @XmlAttribute
   public double scorePercentage;
   @XmlAttribute
   public double avgScore;
   
   @XmlElement
   public long numWithSchemaLocation;
   @XmlElement
   public long numSchemaCRResident;
   @XmlElement
   public long numWithMdProfile;
   @XmlElement
   public long numWithMdSelflink;
   @XmlElement   
   public long numWithMdCollectionDisplayName;
   
   @XmlElementWrapper(name = "duplicatedMDSelfLinks")
   @XmlElement(name = "duplicatedMDSelfLink")
   public Collection<String> duplicatedMDSelfLink;

}

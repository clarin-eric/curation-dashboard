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
public class InstanceHeaderReport {
   @XmlAttribute
   public static final double maxScore = 5.0;
   @XmlAttribute
   public double score;
   @XmlElement
   public String schemaLocation;
   @XmlElement
   public boolean isCRResident;
   @XmlElement   
   public String mdProfile;
   @XmlElement   
   public String mdCollectionDisplayName;
   @XmlElement   
   public String mdSelfLink;
}

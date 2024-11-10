/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import jakarta.xml.bind.annotation.*;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;


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
   public long numSchemaCrResident;
   @XmlElement
   public long numWithMdProfile;
   @XmlElement
   public long numWithMdSelflink;
   @XmlElement
   public long numWithUniqueMdSelflink;
   @XmlElement   
   public long numWithMdCollectionDisplayName;
   
   @XmlElementWrapper(name = "duplicatedMDSelfLinks")
   public Collection<MDSelfLink> duplicatedMDSelfLink = new ArrayList<MDSelfLink>();
   
   @XmlRootElement
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class MDSelfLink {
      
      @XmlElement
      public final String mdSelfLink;
      
      @XmlElementWrapper(name = "origins")
      @XmlElement(name = "origin")
      public final Collection<String> origin;
   }

}

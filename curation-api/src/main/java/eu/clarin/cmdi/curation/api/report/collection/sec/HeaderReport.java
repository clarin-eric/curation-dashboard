/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

import java.util.ArrayList;
import java.util.Collection;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


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

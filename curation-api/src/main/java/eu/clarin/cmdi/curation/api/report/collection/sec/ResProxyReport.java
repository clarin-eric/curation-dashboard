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
public class ResProxyReport {
   @XmlAttribute
   public double aggregatedScore;
   @XmlAttribute
   public double aggregatedMaxScore;
   @XmlAttribute
   public double scorePercentage;
   @XmlAttribute
   public double avgScore;  
   @XmlElement
   public int totNumOfResources;
   @XmlElement
   public double avgNumOfResources;
   @XmlElement
   public int totNumOfResourcesWithMime;
   @XmlElement
   public double avgNumOfResourcesWithMime;
   @XmlElement
   public int totNumOfResourcesWithReference;
   @XmlElement
   public double avgNumOfResourcesWithReference;
   @XmlElementWrapper(name = "invalidReferences")
   @XmlElement(name = "invalidReference")
   public Collection<InvalidReference> invalidReferences = new ArrayList<InvalidReference>(); 
   
   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)  
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class InvalidReference{
      @XmlAttribute
      public final String origin;
      @XmlElementWrapper(name = "references")
      @XmlElement(name = "reference")
      public final Collection<String> references; 
   }
}

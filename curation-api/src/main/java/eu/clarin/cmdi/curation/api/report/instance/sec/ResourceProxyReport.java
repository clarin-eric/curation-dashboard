/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.instance.sec;

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
public class ResourceProxyReport {
   @XmlAttribute
   public static final double maxScore = 2.0;
   @XmlAttribute
   public double score;
   @XmlElement
   public int numOfResources;
   @XmlElement
   public int numOfResourcesWithMime;
   @XmlElement
   public double percOfResourcesWithMime;
   @XmlElement
   public int numOfResourcesWithReference;
   @XmlElement
   public double percOfResourcesWithReference;
   @XmlElementWrapper(name = "resourceTypes")
   @XmlElement(name = "resourceType")
   public Collection<ResourceType> resourceTypes = new ArrayList<ResourceType>();
   @XmlElementWrapper(name = "invalidReferences")
   @XmlElement(name = "invalidReference")   
   public Collection<String> invalidReferences = new ArrayList<String>();



   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class ResourceType {
      @XmlAttribute
      public final String type;
      @XmlAttribute
      public final int count;
   }
}

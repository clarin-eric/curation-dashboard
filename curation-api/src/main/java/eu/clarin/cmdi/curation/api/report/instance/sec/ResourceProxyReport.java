/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.instance.sec;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

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
   public int numOfResProxies;
   @XmlElement
   public int numOfResourcesWithMime;
   @XmlElement
   public double percOfResourcesWithMime;
   @XmlElement
   public int numOfResProxiesWithReference;
   @XmlElement
   public double percOfResProxiesWithReference;
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

/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.collection.sec;

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
public class ResProxyReport {
   @XmlAttribute
   public static final double maxScore = 2.0;
   @XmlAttribute
   public double avgScore;
   @XmlElement
   public int totNumOfResProxies;
   @XmlElement
   public double avgNumOfResProxies;
   @XmlElement
   public int totNumOfResProxiesWithMime;
   @XmlElement
   public double avgNumOfResProxiesWithMime;
   @XmlElement
   public int totNumOfResProxiesWithReference;
   @XmlElement
   public double avgNumOfResProxiesWithReference;
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

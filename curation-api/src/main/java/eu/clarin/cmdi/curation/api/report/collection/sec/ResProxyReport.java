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

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ResProxyReport {
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
   @XmlElementWrapper(name = "invalid-references")
   private Collection<InvalidReference> invalidReferences = new ArrayList<InvalidReference>(); 
   
   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)   
   public static class InvalidReference{
      @XmlAttribute
      public String reference;
      @XmlAttribute
      public String origin;      
   }
}

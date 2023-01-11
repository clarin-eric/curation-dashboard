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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Getter
public class ResProxyReport {
   private long numOfFiles;
   private long totNumOfResProxies;
   private long totNumOfResourcesWithMime;
   private int totNumOfResProxiesWithReferences;
   @XmlElementWrapper(name = "invalid-references")
   private Collection<InvalidReference> invalidReferences = new ArrayList<InvalidReference>(); 
   
   public void addTotNumOfResProxies(long addition) {
      this.numOfFiles++;
      this.totNumOfResProxies+=addition;
   }
   public void addTotNumOfResourcesWithMime(long addition) {
      this.totNumOfResourcesWithMime+=addition;
   }   
   public void addTotNumOfResProxiesWithReferences(long addition) {
      this.totNumOfResProxiesWithReferences+=addition;
   } 
   
   public double getAvgNumOfResProxies() {
      return this.numOfFiles!=0?this.totNumOfResProxies/this.numOfFiles:0.0;
   }
   
   public double getAvgNumOfResourcesWithMime() {
      return this.numOfFiles!=0?this.getAvgNumOfResourcesWithMime()/this.numOfFiles:0.0;
   }
   public double getAvgNumOfResProxiesWithReferences() {
      return this.numOfFiles!=0?this.totNumOfResProxiesWithReferences/this.numOfFiles:0.0;
   }
   
   
   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)   
   public static class InvalidReference{
      @XmlAttribute
      public String reference;
      @XmlAttribute
      public String origin;      
   }
}

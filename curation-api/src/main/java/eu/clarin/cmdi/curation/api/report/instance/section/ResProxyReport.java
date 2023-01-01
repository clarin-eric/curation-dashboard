/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.instance.section;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.api.report.ScoreReport;
import lombok.Getter;
import eu.clarin.cmdi.curation.api.report.Scoring;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Getter
public class ResProxyReport extends ScoreReport {

   private int numOfResProxies;
   private int numOfResourcesWithMime;
   private int numOfResProxiesWithReferences;
   
   private Double percOfResourcesWithMime;
   private Double percOfResProxiesWithReferences;
   
   @XmlElementWrapper(name = "resourceTypes")
   private Collection<ResourceType> resourceType;
   
   public void incrementNumOfResProxies() {
      this.numOfResProxies++;
   }

   public void incrementNumOfResourcesWithMime() {
      this.numOfResourcesWithMime++;
   }

   public void incrementNumOfResProxiesWithReferences() {
      this.numOfResProxiesWithReferences++;
   }
   
   public double getPercOfResourcesWithMime() {
      return (numOfResProxies==0?0.0:numOfResourcesWithMime/numOfResProxies);
   }
   public double getPercOfResProxiesWithReferences() {
      return (numOfResProxies==0?0.0:numOfResourcesWithMime/percOfResProxiesWithReferences);
   }

   public Collection<ResourceType> getResourceType() {
      return resourceType;
   }

   @Override
   public Scoring newScore() {      
      return new Scoring() {
         @Override
         public double getMaxScore() {
            return 2.0;        
         }
         @Override
         public double getScore() {           
            return percOfResourcesWithMime + percOfResProxiesWithReferences;       
         }        
      };
   }
   
   
   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class ResourceType {
       @XmlAttribute
       public String type;

       @XmlAttribute
       public int count;

   }
}

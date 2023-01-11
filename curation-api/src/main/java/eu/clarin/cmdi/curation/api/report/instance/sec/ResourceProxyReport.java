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

import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.api.report.Scoring;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class ResourceProxyReport extends ScoreReport {

   private int numOfResProxies;
   private int numOfResourcesWithMime;
   private int numOfResProxiesWithReferences;
   private Collection<ResourceType> resourceTypes = new ArrayList<ResourceType>();

   @XmlElement
   public int getNumOfResProxies() {
      return this.numOfResProxies;
   }

   @XmlElement
   public int getNumOfResourcesWithMime() {
      return numOfResourcesWithMime;
   }

   @XmlElement
   public double getPercOfResourcesWithMime() {
      return (numOfResProxies == 0 ? 0.0 : numOfResourcesWithMime / numOfResProxies);
   }

   @XmlElement
   public int getNumOfResProxiesWithReferences() {
      return numOfResProxiesWithReferences;
   }

   @XmlElement
   public double getPercOfResProxiesWithReferences() {
      return (numOfResProxies == 0 ? 0.0 : numOfResourcesWithMime / getPercOfResProxiesWithReferences());
   }

   @XmlElementWrapper(name = "resourceTypes")
   public Collection<ResourceType> getResourceTypes() {
      return this.resourceTypes;
   }

   public void addResourceType(String typeName, int count) {
      this.resourceTypes.add(new ResourceType(typeName, count));
   }

   public void incrementNumOfResProxies() {
      this.numOfResProxies++;
   }

   public void incrementNumOfResourcesWithMime() {
      this.numOfResourcesWithMime++;
   }

   public void incrementNumOfResProxiesWithReferences() {
      this.numOfResProxiesWithReferences++;
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.NONE)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public class ResourceType {

      private final String type;
      private final int count;

      @XmlAttribute
      public String getType() {
         return type;
      }
      @XmlAttribute
      public int getCount() {
         return count;
      }
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
            return getPercOfResourcesWithMime() + getPercOfResProxiesWithReferences();
         }
      };
   }
}

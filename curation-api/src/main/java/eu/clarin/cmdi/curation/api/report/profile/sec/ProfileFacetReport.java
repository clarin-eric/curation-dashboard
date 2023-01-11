/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile.sec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.api.report.Scoring;


/**
 *
 */
@XmlRootElement(name = "facets")
@XmlAccessorType(XmlAccessType.NONE)
public class ProfileFacetReport extends ScoreReport {
   
   private Map<String, Coverage> coverageMap = new HashMap<String, Coverage>();

   @XmlAttribute
   public int getNumOfFacets() {
      return this.coverageMap.size();
   };
   @XmlAttribute
   public double getProfileCoverage() {
      return this.coverageMap.values().stream().filter(Coverage::isCoveredByProfile).count();
   };
   @XmlElementWrapper
   @XmlElement(name = "facet")
   public Collection<Coverage> getCoverage(){
      return this.coverageMap.values();
   }
   
   public Coverage getCoverage(String facetName) {
      return this.coverageMap.computeIfAbsent(facetName, Coverage::new);
   }


   @XmlRootElement
   @XmlAccessorType(XmlAccessType.NONE)
   public class Coverage {
      
      public Coverage(String name) {
         this.name = name;
      }

      private final String name;

      private boolean coveredByProfile;
      
      @XmlAttribute
      public String getName() {
         return this.name;
      }
      @XmlAttribute
      public boolean isCoveredByProfile() {
         return this.coveredByProfile;
      }
      
      public void setCoveredByProfile(boolean coveredByProfile) {
         this.coveredByProfile = coveredByProfile;
      }
   }

   @Override
   public Scoring newScore() {
      return new Scoring() {
         @Override
         public double getMaxScore() {
            return 1.0;
         }
         @Override
         public double getScore() {
            return (ProfileFacetReport.this.getNumOfFacets()!=0?ProfileFacetReport.this.getProfileCoverage()/ProfileFacetReport.this.getNumOfFacets():0.0);
         }         
      };
   }
}
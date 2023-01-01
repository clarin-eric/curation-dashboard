/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.report.profile.section;

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
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 *
 */
@XmlRootElement(name = "facets")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Getter
@Setter
public class ProfileFacetReport extends ScoreReport {

   @XmlAttribute
   private int numOfFacets;

   @XmlElementWrapper
   @XmlElement(name = "facet")
   private Collection<Coverage> coverage = new ArrayList<Coverage>();

   @XmlAttribute
   public double getProfileCoverage() {
      return this.coverage.stream().filter(Coverage::isCoveredByProfile).count();
   };


   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @Data
   public static class Coverage {

      @XmlAttribute
      private String name;
      @XmlAttribute
      private boolean coveredByProfile;
      

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
            return (ProfileFacetReport.this.numOfFacets!=0?ProfileFacetReport.this.getProfileCoverage()/ProfileFacetReport.this.numOfFacets:0.0);
         }         
      };
   }
}
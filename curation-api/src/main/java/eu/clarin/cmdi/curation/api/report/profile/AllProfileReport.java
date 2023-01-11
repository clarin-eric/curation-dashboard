package eu.clarin.cmdi.curation.api.report.profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.clarin.cmdi.curation.api.report.AggregationReport;
import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.api.report.Scoring;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileFacetReport.Coverage;


@XmlRootElement(name = "profiles")
@XmlAccessorType(XmlAccessType.FIELD)
public class AllProfileReport extends ScoreReport implements AggregationReport<CMDProfileReport>, NamedReport {
   @XmlAttribute(name = "creation-time")
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public LocalDateTime creationTime = LocalDateTime.now();
   @XmlElement(name = "profile")
   private List<Profile> profiles = new ArrayList<Profile>();

   @Override
   public String getName() {

      return this.getClass().getSimpleName();
   
   }

   @Override
   public boolean isValid() {

      return false;
   }


   @Override
   public void addReport(CMDProfileReport profileReport) {

      this.profiles.add(new Profile(profileReport));

   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
   public static class Profile {
      
      private CMDProfileReport report;

      @XmlAttribute
      public String getId() {
         return report.getHeaderReport().getId();
      };
      @XmlElement
      public String getName() {
         return report.getHeaderReport().getName();
      };
      @XmlElement
      public String getReportName() {
         return report.getName();
      };
      @XmlElement
      public double getScore() {
         return report.getScoring().getScore();
      };
      @XmlElement
      public double getFacetCoverage() {
         return report.getFacetReport().getProfileCoverage();
      };
      @XmlElement
      public double getPercOfElementsWithConcept() {
         return report.getConceptReport().getPercWithConcept();
      };

      @XmlElementWrapper(name = "facets")
      @XmlElement(name = "facet")
      public Collection<Coverage> getFacets(){
         return report.getFacetReport().getCoverage();
      }
      @XmlElement
      public double getCollectionUsage() {
         return report.getCollectionUsage().size();
      };
      @XmlElement
      public double getInstanceUsage() {
         return report.getCollectionUsage().stream().mapToDouble(usage -> usage.count).sum();
      };

      public Profile() {

      }

      public Profile(CMDProfileReport report) {
         this.report = report;
      }
   }

   @Override
   public Scoring newScore() {
      return null;
   }
}

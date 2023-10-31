package eu.clarin.cmdi.curation.api.report.profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.clarin.cmdi.curation.api.report.AggregationReport;
import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.Detail.Severity;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileFacetReport.Coverage;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AllProfileReport implements AggregationReport<CMDProfileReport>, NamedReport {
   @XmlAttribute
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public LocalDateTime creationTime = LocalDateTime.now();
   @XmlElement(name = "profile")
   private List<Profile> profiles = new ArrayList<Profile>();

   @Override
   public String getName() {

      return this.getClass().getSimpleName();
   
   }


   @Override
   public void addReport(CMDProfileReport profileReport) {
      
      if(!profileReport.details.stream().anyMatch(message -> message.severity == Severity.FATAL)) {
         this.profiles.add(new Profile(profileReport));
      }
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class Profile {

      private final CMDProfileReport report;

      @XmlAttribute
      public String getId() {
         return report.headerReport.getId();
      };
      @XmlElement
      public String getName() {
         return report.headerReport.getName();
      };
      @XmlElement
      public String getReportName() {
         return report.getName();
      };
      @XmlElement
      public double getScore() {
         return report.score;
      };
      @XmlElement
      public double getFacetCoverage() {
         return report.facetReport.percProfileCoverage;
      };
      @XmlElement
      public double getPercOfElementsWithConcept() {
         return report.conceptReport.percWithConcept;
      };

      @XmlElementWrapper(name = "facets")
      @XmlElement(name = "facet")
      public Collection<Coverage> getFacets(){
         return report.facetReport.coverages;
      }
      @XmlElement
      public double getCollectionUsage() {
         return report.collectionUsage.size();
      };
      @XmlElement
      public double getInstanceUsage() {
         return report.collectionUsage.stream().mapToDouble(usage -> usage.count).sum();
      };
   }
}

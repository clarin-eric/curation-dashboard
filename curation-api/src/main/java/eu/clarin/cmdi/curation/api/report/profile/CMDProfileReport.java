package eu.clarin.cmdi.curation.api.report.profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.api.report.Scoring;
import eu.clarin.cmdi.curation.api.report.profile.sec.ComponentReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ConceptReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileFacetReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileHeaderReport;
import lombok.Getter;
import lombok.Setter;

/**
 * A selection of values from a single CMDProfileReport which will form a line
 * in a statistical overview
 */
@XmlRootElement(name = "profile-report")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class CMDProfileReport extends ScoreReport implements NamedReport{

   @XmlAttribute(name = "creation-time")
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   private LocalDateTime creationTime = LocalDateTime.now();

   @XmlElement(name = "header-section")
   private ProfileHeaderReport headerReport;

   @XmlElement(name = "cmd-components-section")
   private ComponentReport componentReport;

   @XmlElement(name = "cmd-concepts-section")
   private ConceptReport conceptReport;

   @XmlElement(name = "facets-section")
   private ProfileFacetReport facetReport;
   
   

   @XmlElementWrapper(name = "usage-section")
   @XmlElement(name = "collection")
   private Collection<CollectionUsage> collectionUsage = new ArrayList<CollectionUsage>();


   public void addCollectionUsage(String collectionName, long count) {
      this.collectionUsage.add(new CollectionUsage(collectionName, count));
   }

   @Override
   public String getName() {
      return (this.headerReport!=null?this.headerReport.getId():"missing header report");
   }



   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class CollectionUsage {
      public CollectionUsage() {
      }

      public CollectionUsage(String collectionName, long count) {
         this.collectionName = collectionName;
         this.count = count;
      }

      @XmlAttribute
      public String collectionName;

      @XmlAttribute
      public long count;

   }

   @Override
   public boolean isValid() {
      return getSectionReports().filter(sectionReport -> !sectionReport.isValid()).findAny().isEmpty();
   }

   @Override
   public Scoring newScore() {
      return new Scoring() {
         @Override
         public double getMaxScore() {
            return getSectionReports().mapToDouble(sectionReport -> sectionReport.getScoring().getMaxScore()).sum();
         }
         @Override
         public double getScore() {
            return getSectionReports().mapToDouble(sectionReport -> sectionReport.getScoring().getScore()).sum();
         }
      };
   }
   
   private Stream<ScoreReport> getSectionReports(){
      return Stream.of(this.headerReport, this.componentReport, this.conceptReport, this.facetReport).filter(report -> report != null);
   } 
}

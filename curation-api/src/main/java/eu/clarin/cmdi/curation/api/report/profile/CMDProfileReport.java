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
import eu.clarin.cmdi.curation.api.report.Score;

/**
 * A selection of values from a single CMDProfileReport which will form a line
 * in a statistical overview
 */
@XmlRootElement(name = "profile-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDProfileReport extends ScoreReport implements NamedReport{

   @XmlAttribute(name = "max-score")
   public double maxScore;

   @XmlAttribute(name = "score")
   public Double score = 0.0;

   @XmlAttribute(name = "creation-time")
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public LocalDateTime creationTime = LocalDateTime.now();

   @XmlElement(name = "header-section")
   public HeaderReport headerReport = new HeaderReport();

   @XmlElement(name = "cmd-components-section")
   public ComponentReport componentReport = new ComponentReport();

   @XmlElement(name = "cmd-concepts-section")
   public ConceptReport conceptReport = new ConceptReport();

   @XmlElement(name = "facets-section")
   public FacetReport facetReport = new FacetReport();
   
   

   @XmlElementWrapper(name = "usage-section")
   @XmlElement(name = "collection")
   public Collection<CollectionUsage> collectionUsage = new ArrayList<CollectionUsage>();


   public void addCollectionUsage(String collectionName, long count) {
      this.collectionUsage.add(new CollectionUsage(collectionName, count));
   }

   @Override
   public String getName() {
      return headerReport.getId();
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
   public Score newScore() {
      return new Score() {
         @Override
         public double getMax() {
            return getSectionReports().mapToDouble(sectionReport -> sectionReport.getScore().getMax()).sum();
         }
         @Override
         public double getCurrent() {
            return getSectionReports().mapToDouble(sectionReport -> sectionReport.getScore().getCurrent()).sum();
         }
      };
   }
   
   private Stream<ScoreReport> getSectionReports(){
      return Stream.of(this.headerReport, this.componentReport, this.conceptReport, this.facetReport);
   }
   
   
}

package eu.clarin.cmdi.curation.api.report.collection;

import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AllCollectionReport implements NamedReport {

   @XmlAttribute
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public LocalDateTime creationTime = LocalDateTime.now();

   @XmlAttribute
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public LocalDateTime previousCreationTime;

   @XmlElement(name = "collection")
   private Collection<CollectionReportWrapper> collectionReports = new ArrayList<CollectionReportWrapper>();


   @Override
   @XmlTransient
   public String getName() {

      return this.getClass().getSimpleName();
      
   }

   @Override
   @XmlTransient
   public LocalDateTime getCreationTime() {

      return this.creationTime;
   }

   @Override
   public void setPreviousCreationTime(LocalDateTime previousCreationTime) {

      this.previousCreationTime = previousCreationTime;
   }

   @Override
   @XmlTransient
   public LocalDateTime getPreviousCreationTime() {

      return this.previousCreationTime;
   }

   public void addReport(CollectionReport report) {
      
      this.collectionReports.add(new CollectionReportWrapper((CollectionReport) report));
   
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class CollectionReportWrapper {
      
      private final CollectionReport collectionReport;
      @XmlAttribute
      public String getName() {
         return collectionReport.getName();
      }
      @XmlElement
      public String getReportName() {
         return collectionReport.getName();
      }
      @XmlElement
      public double getScorePercentage() {
         return collectionReport.scorePercentage;
      };
      @XmlElement
      public long getNumOfFiles() {
         return collectionReport.fileReport.numOfFiles;
      }
      @XmlElement
      public int getNumOfProfiles() {
         return collectionReport.profileReport.totNumOfProfiles;
      }
      @XmlElement
      public int getNumOfLinks() {
         return collectionReport.linkcheckerReport.totNumOfLinks;
      }
      @XmlElement
      public int getNumOfUniqueLinks() {
         return collectionReport.linkcheckerReport.totNumOfUniqueLinks;
      }
      @XmlElement
      public int getNumOfCheckedLinks() {
         return collectionReport.linkcheckerReport.totNumOfCheckedLinks;
      }
      @XmlElement
      public double getRatioOfValidLinks() {
         return collectionReport.linkcheckerReport.ratioOfValidLinks;
      }
      @XmlElement
      public double getAvgNumOfResources() {
         return collectionReport.resProxyReport.avgNumOfResources;
      }
      @XmlElement
      public long getNumOfResources() {
         return collectionReport.resProxyReport.totNumOfResources;
      }
      @XmlElement
      public double getRatioOfValidRecords() {
         return collectionReport.xmlValidityReport.avgScore;
      }
      @XmlElement
      public double getAvgNumOfEmptyXMLElements() {
         return collectionReport.xmlPopulationReport.avgNumOfXMLEmptyElements;
      }
      @XmlElement
      private double getAvgFacetCoverage() {
         return collectionReport.facetReport.avgScore;
      }

      @XmlElementWrapper(name = "facets")
      @XmlElement(name = "facet")
      public Collection<eu.clarin.cmdi.curation.api.report.collection.sec.FacetReport.FacetCollectionStruct> getFacets(){
         return collectionReport.facetReport.facets;
      }
   }
}

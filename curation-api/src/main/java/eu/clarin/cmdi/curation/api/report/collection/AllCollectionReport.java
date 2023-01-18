package eu.clarin.cmdi.curation.api.report.collection;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@XmlRootElement(name = "collections-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class AllCollectionReport implements NamedReport {

   @XmlAttribute(name = "creation-time")
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public LocalDateTime creationTime = LocalDateTime.now();

   @XmlElement(name = "collection")
   private List<Collection> collections = new ArrayList<Collection>();


   @Override
   public String getName() {

      return this.getClass().getSimpleName();
      
   }

   public void addReport(CollectionReport report) {
      
      this.collections.add(new Collection((CollectionReport) report));
   
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class Collection {
      @XmlAttribute
      private String name;
      @XmlElement
      private String reportName;
      @XmlElement
      private double scorePercentage;
      @XmlElement
      private long numOfFiles;
      @XmlElement
      private int numOfProfiles;
      @XmlElement
      private int numOfLinks;
      @XmlElement
      private int numOfCheckedLinks;
      @XmlElement
      private double ratioOfValidLinks;
      @XmlElement
      private double avgNumOfResProxies;
      @XmlElement
      private long numOfResProxies;
      @XmlElement
      private double ratioOfValidRecords;
      @XmlElement
      private double avgNumOfEmptyXMLElements;
      @XmlElement
      private double avgFacetCoverage;

      @XmlElementWrapper(name = "facets")
      @XmlElement(name = "facet")
      private List<Facet> facets = new ArrayList<Facet>();

      public Collection() {

      }

      public Collection(CollectionReport report) {
         this.name = report.getName();
         this.reportName = report.getName();
//         this.scorePercentage = report.getscorePercentage();
         this.numOfFiles = report.fileReport.numOfFiles;
         this.numOfProfiles = report.headerReport.totNumOfProfiles;
         this.numOfLinks = report.linkcheckerReport.totNumOfLinks;
         this.numOfCheckedLinks = report.linkcheckerReport.totNumOfCheckedLinks;
         this.ratioOfValidLinks = report.linkcheckerReport.ratioOfValidLinks;
         this.avgNumOfResProxies = report.resProxyReport.avgNumOfResProxies;
         this.numOfResProxies = report.resProxyReport.totNumOfResProxies;
         this.ratioOfValidRecords = report.xmlValidationReport.ratioOfValidRecords;
         this.avgNumOfEmptyXMLElements = report.xmlPopulationReport.avgNumOfXMLEmptyElements;
         this.avgFacetCoverage = report.facetReport.percCoverageNonZero;

         report.facetReport.facets.forEach(f -> this.facets.add(new Facet(f.name, f.coverage)));
      }

   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class Facet {
      @XmlAttribute
      private final String name;
      @XmlElement
      private final double avgCoverage;

   }
}

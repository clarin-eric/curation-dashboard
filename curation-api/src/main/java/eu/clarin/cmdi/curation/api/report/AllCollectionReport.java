package eu.clarin.cmdi.curation.api.report;

import eu.clarin.cmdi.curation.api.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "collections-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class AllCollectionReport extends Report<CollectionReport> {

   @XmlAttribute(name = "creation-time")
   public String creationTime = TimeUtils.humanizeToDate(System.currentTimeMillis());

   @XmlElement(name = "collection")
   private List<Collection> collections = new ArrayList<Collection>();


   @Override
   public String getName() {

      return "CollectionsReport";
   }

   @Override
   public boolean isValid() {

      return false;
   }

   @Override
   public void addSegmentScore(Score segmentScore) {

   }


   @Override
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
      private int numOfUniqueLinks;
      @XmlElement
      private int numOfCheckedLinks;
      @XmlElement
      private double ratioOfValidLinks;
      @XmlElement
      private double avgNumOfResProxies;
      @XmlElement
      private int numOfResProxies;
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
         this.scorePercentage = report.scorePercentage;
         this.numOfFiles = report.fileReport.numOfFiles;
         this.numOfProfiles = report.headerReport.profiles.totNumOfProfiles;
         this.numOfUniqueLinks = report.urlReport.totNumOfUniqueLinks;
         this.numOfCheckedLinks = report.urlReport.totNumOfCheckedLinks;
         this.ratioOfValidLinks = report.urlReport.ratioOfValidLinks;
         this.avgNumOfResProxies = report.resProxyReport.avgNumOfResProxies;
         this.numOfResProxies = report.resProxyReport.totNumOfResProxies;
         this.ratioOfValidRecords = report.xmlValidationReport.ratioOfValidRecords;
         this.avgNumOfEmptyXMLElements = report.xmlPopulatedReport.avgXMLEmptyElement;
         this.avgFacetCoverage = report.facetReport.coverage;

         report.facetReport.facet.forEach(f -> this.facets.add(new Facet(f.name, f.coverage)));
      }

   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class Facet {
      @XmlAttribute
      private String name;
      @XmlElement
      private double avgCoverage;

      public Facet() {

      }

      public Facet(String name, double avgCoverage) {
         this.name = name;
         this.avgCoverage = avgCoverage;
      }
   }
}

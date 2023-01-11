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

import eu.clarin.cmdi.curation.api.report.AggregationReport;
import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;

@XmlRootElement(name = "collections-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class AllCollectionReport implements AggregationReport<CollectionReport>, NamedReport {

   @XmlAttribute(name = "creation-time")
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public LocalDateTime creationTime = LocalDateTime.now();

   @XmlElement(name = "collection")
   private List<Collection> collections = new ArrayList<Collection>();


   @Override
   public String getName() {

      return this.getClass().getSimpleName();
      
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
         this.numOfFiles = report.getFileReport().getNumOfFiles();
         this.numOfProfiles = report.getHeaderReport().getTotNumOfProfiles();
         this.numOfLinks = report.getLinkcheckerReport().getTotNumOfLinks();
         this.numOfCheckedLinks = report.getLinkcheckerReport().getTotNumOfCheckedLinks();
         this.ratioOfValidLinks = report.getLinkcheckerReport().getRatioOfValidLinks();
         this.avgNumOfResProxies = report.getResProxyReport().getAvgNumOfResProxies();
         this.numOfResProxies = report.getResProxyReport().getTotNumOfResProxies();
         this.ratioOfValidRecords = report.getXmlValidationReport().getRatioOfValidRecords();
         this.avgNumOfEmptyXMLElements = report.getXmlPopulationReport().getAvgXMLEmptyElement();
         this.avgFacetCoverage = report.getFacetReport().getAvgCoverage();

         report.getFacetReport().getFacets().forEach(f -> this.facets.add(new Facet(f.getName(), f.getCoverage())));
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

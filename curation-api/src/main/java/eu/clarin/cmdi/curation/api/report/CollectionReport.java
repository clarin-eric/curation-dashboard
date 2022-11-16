package eu.clarin.cmdi.curation.api.report;

import eu.clarin.cmdi.cpa.utils.Category;
import eu.clarin.cmdi.curation.api.utils.TimeUtils;
import eu.clarin.cmdi.curation.api.xml.XMLMarshaller;
import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.annotation.*;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

/**
 *
 */

/**
 * report for one single collection
 *
 */
@XmlRootElement(name = "collection-report")
@XmlAccessorType(XmlAccessType.FIELD)
@Slf4j
public class CollectionReport implements Report<CollectionReport> {
   
   @XmlAttribute(name = "score")
   public Double score = 0.0;

   @XmlAttribute(name = "avg-score")
   public Double avgScore = 0.0;

   @XmlAttribute(name = "min-score")
   public Double insMinScore = Double.MAX_VALUE;

   @XmlAttribute(name = "max-score")
   public Double insMaxScore = 0.0;

   @XmlAttribute(name = "col-max-score")
   public Double maxScore = 0.0;

   @XmlAttribute(name = "score-percentage")
   public Double scorePercentage;

   @XmlAttribute(name = "ins-max-score")
   public Double maxPossibleScoreInstance = 0.0;

   @XmlAttribute(name = "creation-time")
   public String creationTime = TimeUtils.humanizeToDate(System.currentTimeMillis());

   @XmlElement(name = "file-section")
   public FileReport fileReport;

   @XmlElement(name = "header-section")
   public HeaderReport headerReport;

   // ResProxies
   @XmlElement(name = "resProxy-section")
   public ResProxyReport resProxyReport;

   // XMLPopulatedValidator
   @XmlElement(name = "xml-populated-section")
   public XMLPopulatedReport xmlPopulatedReport;

   // XMLValidator
   @XmlElement(name = "xml-validation-section")
   public XMLValidationReport xmlValidationReport;

   // URL
   @XmlElement(name = "url-validation-section")
   public URLValidationReport urlReport;

   // Facets
   @XmlElement(name = "facet-section")
   public FacetReport facetReport;

   // Invalid Files
   @XmlElementWrapper(name = "invalid-files")
   public Collection<InvalidFile> file;

   @XmlRootElement
   public static class InvalidFile {
      @XmlValue
      public String recordName;

      @XmlAttribute(name = "reason")
      public String reason;
   }
   
   public CollectionReport() {
      
      this.fileReport = new FileReport();
      this.headerReport = new HeaderReport();
      this.resProxyReport = new ResProxyReport();
      this.xmlPopulatedReport = new XMLPopulatedReport();
      this.xmlValidationReport = new XMLValidationReport();
      this.urlReport = new URLValidationReport();
      this.facetReport = new FacetReport();
      
   }

   public void addInvalidFile(InvalidFile invalidFile) {
      if (this.file == null) {
         this.file = new ArrayList<>();
      }
      this.file.add(invalidFile);
   }

   public void handleProfile(String profile, double score) {
      if (headerReport == null) {
         headerReport = new HeaderReport();
      }
      if (headerReport.profiles == null) {
         headerReport.profiles = new Profiles();
      }
      headerReport.profiles.handleProfile(profile, score);
   }

   @Override
   public String getName() {
      return fileReport.provider;
   }

   @Override
   public void setParentName(String parentName) {
      // don't do anything, there is no parent to a collection
   }

   @Override
   public String getParentName() {
      return null;
   }

   @Override
   public void mergeWithParent(CollectionReport parentReport) {
      log.error("this should never happen??? a collection report cant have a parent to get merged into");

   }

   @Override
   public void addSegmentScore(Score segmentScore) {
      // not used
   }

   @Override
   public boolean isValid() {
      return true;
   }

   public void calculateAverageValues() {

      // file
      fileReport.avgSize = fileReport.size / fileReport.numOfFiles;

      // ResProxies
      resProxyReport.avgNumOfResProxies = (double) resProxyReport.totNumOfResProxies / fileReport.numOfFiles;
      resProxyReport.avgNumOfResourcesWithMime = (double) resProxyReport.totNumOfResourcesWithMime
            / fileReport.numOfFiles;
      resProxyReport.avgNumOfResProxiesWithReferences = (double) resProxyReport.totNumOfResProxiesWithReferences
            / fileReport.numOfFiles;

      // XMLValidator
      xmlValidationReport.ratioOfValidRecords = xmlValidationReport.totNumOfRecords == 0 ? 0
            : (double) xmlValidationReport.totNumOfValidRecords / xmlValidationReport.totNumOfRecords;

      // XMLPopulatedValidator
      xmlPopulatedReport.avgNumOfXMLElements = (double) xmlPopulatedReport.totNumOfXMLElements / fileReport.numOfFiles;
      xmlPopulatedReport.avgNumOfXMLSimpleElements = (double) xmlPopulatedReport.totNumOfXMLSimpleElements
            / fileReport.numOfFiles;
      xmlPopulatedReport.avgXMLEmptyElement = (double) xmlPopulatedReport.totNumOfXMLEmptyElement
            / fileReport.numOfFiles;

      xmlPopulatedReport.avgRateOfPopulatedElements = xmlPopulatedReport.avgNumOfXMLSimpleElements == 0 ? 0
            : (xmlPopulatedReport.avgNumOfXMLSimpleElements - xmlPopulatedReport.avgXMLEmptyElement)
                  / xmlPopulatedReport.avgNumOfXMLSimpleElements;

      // url statistics

      // done by sql query in CollectionAggregator

      int totCheckedUndeterminedAndRestrictedAndBlockedRemoved = urlReport.totNumOfCheckedLinks
            - (urlReport.totNumOfUndeterminedLinks + urlReport.totNumOfRestrictedAccessLinks
                  + urlReport.totNumOfBlockedByRobotsTxtLinks);

      urlReport.ratioOfValidLinks = urlReport.totNumOfCheckedLinks == 0 ? 0
            : (double) (totCheckedUndeterminedAndRestrictedAndBlockedRemoved - urlReport.totNumOfBrokenLinks)
                  / totCheckedUndeterminedAndRestrictedAndBlockedRemoved;

      // Facets
      facetReport.facet.forEach(facet -> facet.coverage = (double) facet.cnt / fileReport.numOfFiles);
      facetReport.coverage = facetReport.facet.stream().mapToDouble(f -> f.cnt != 0 ? 1.0 : 0).sum()
            / facetReport.facet.size();

      avgScore = fileReport.numOfFiles == 0 ? 0.0 : score / fileReport.numOfFiles;
      maxScore = fileReport.numOfFiles * maxPossibleScoreInstance;

      scorePercentage = maxScore == 0.0 ? 0.0 : score / maxScore;

   }

   @Override
   public void toXML(OutputStream os) {
      XMLMarshaller<CollectionReport> instanceMarshaller = new XMLMarshaller<>(CollectionReport.class);
      instanceMarshaller.marshal(this, os);
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class FileReport {
      public String provider;
      public long numOfFiles = 0;
      public long size;
      public long avgSize;
      public long minFileSize;
      public long maxFileSize;
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class HeaderReport {
      @XmlElementWrapper(name = "duplicatedMDSelfLinks")
      public Collection<String> duplicatedMDSelfLink = null;
      public Profiles profiles = new Profiles();
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class ResProxyReport {
      public int totNumOfResProxies;
      public Double avgNumOfResProxies = 0.0;
      public int totNumOfResourcesWithMime;
      public Double avgNumOfResourcesWithMime = 0.0;
      public int totNumOfResProxiesWithReferences;
      public Double avgNumOfResProxiesWithReferences = 0.0;

   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class XMLPopulatedReport {
      public int totNumOfXMLElements;
      public Double avgNumOfXMLElements = 0.0;
      public int totNumOfXMLSimpleElements;
      public Double avgNumOfXMLSimpleElements = 0.0;
      public int totNumOfXMLEmptyElement;
      public Double avgXMLEmptyElement = 0.0;
      public Double avgRateOfPopulatedElements = 0.0;
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class XMLValidationReport {
      public int totNumOfRecords;
      public int totNumOfValidRecords;
      public Double ratioOfValidRecords = 0.0;
      @XmlElementWrapper(name = "invalid-records")
      public Collection<Record> record = new ArrayList<>();
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class Record {
      @XmlAttribute
      public String name;

      @XmlElement(name = "issue")
      public Collection<String> issues;
   }

   @XmlRootElement
   public static class URLValidationReport {
      public int totNumOfLinks;
      public Double avgNumOfLinks = 0.0;
      public int totNumOfUniqueLinks;
      public int totNumOfCheckedLinks;
      public Double avgNumOfUniqueLinks = 0.0;
      public int totNumOfInvalidLinks;
      public int totNumOfBrokenLinks;
      public Double avgNumOfBrokenLinks = 0.0;
      public Double ratioOfValidLinks = 0.0;
      public int totNumOfUndeterminedLinks;
      public int totNumOfRestrictedAccessLinks;
      public int totNumOfBlockedByRobotsTxtLinks;
      public Double avgRespTime = 0.0;
      public Long maxRespTime = 0L;
      @XmlElementWrapper(name = "linkchecker")
      public Collection<Statistics> statistics = new TreeSet<Statistics>();
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class Statistics implements Comparable<Statistics> {
      @XmlAttribute
      public long count;

      @XmlTransient
      public long nonNullCount;

      @XmlAttribute
      public double avgRespTime;

      @XmlAttribute
      public double maxRespTime;

      @XmlAttribute
      public Category category;

      public Statistics() {

      }

      public Statistics(Category category) {
         this.category = category;
      }

      @Override
      public int compareTo(Statistics other) {

         return this.category.compareTo(other.category);
      }
   }

   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class FacetReport {
      public Double coverage = 0.0;

      @XmlElementWrapper(name = "facets")
      public Collection<FacetCollectionStruct> facet;
      
      public FacetReport() {
         
         this.facet = new ArrayList<>();
      
      }
   }

   @XmlRootElement
   public static class FacetCollectionStruct {
      @XmlAttribute
      public String name;

      @XmlAttribute
      public int cnt; // num of records covering it

      @XmlAttribute
      public Double coverage;

   }

   @XmlRootElement(name = "profiles")
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class Profiles {

      @XmlAttribute(name = "count")
      public int totNumOfProfiles = 0;

      public List<Profile> profiles = new ArrayList<>();

      public void handleProfile(String profile, double score) {
         if (profiles == null) {
            profiles = new ArrayList<>();
         }

         for (Profile p : profiles) {
            if (p.name.equals(profile)) {
               p.count++;
               return;
            }
         }

         Profile p = new Profile();
         p.count = 1;
         p.name = profile;
         p.score = score;

         profiles.add(p);
         totNumOfProfiles++;
      }

   }

   @XmlRootElement(name = "profile")
   @XmlAccessorType(XmlAccessType.FIELD)
   public static class Profile {

      @XmlAttribute
      public String name;

      @XmlAttribute
      public int count;

      @XmlAttribute
      public Double score;
   }

}

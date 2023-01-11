package eu.clarin.cmdi.curation.api.report.collection;

import eu.clarin.cmdi.curation.api.report.AggregationReport;
import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.api.report.Scoring;
import eu.clarin.cmdi.curation.api.report.Scoring.Severity;
import eu.clarin.cmdi.curation.api.report.collection.sec.FacetReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.FileReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.HeaderReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.LinkcheckerReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.ResProxyReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.XmlPopulationReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.XmlValidityReport;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport.Coverage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 */

/**
 * report for one single collection
 *
 */
@XmlRootElement(name = "collection-report")
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
public class CollectionReport extends ScoreReport implements AggregationReport<CMDInstanceReport>, NamedReport{
   
   @XmlAttribute(name = "score")
   public Double score = 0.0;

   @XmlAttribute(name = "avg-score")
   public double getAvgScore() {
      return fileReport.getNumOfFiles()!=0?this.score/fileReport.getNumOfFiles():0.0;
   }

   @XmlAttribute(name = "min-score")
   public double insMinScore = Double.MAX_VALUE;

   @XmlAttribute(name = "max-score")
   public double insMaxScore;

   @XmlAttribute(name = "col-max-score")
   public double maxScore;

   @XmlAttribute(name = "score-percentage")
   public double getScorePercentage() {
      return this.maxScore!=0.0?this.score/maxScore:0.0;
   }

   @XmlAttribute(name = "ins-max-score")
   public Double maxPossibleScoreInstance = 0.0;

   @XmlAttribute(name = "creation-time")
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public LocalDateTime creationTime = LocalDateTime.now();

   @XmlElement(name = "file-section")
   private final FileReport fileReport = new FileReport();

   @XmlElement(name = "header-section")
   private final HeaderReport headerReport = new HeaderReport();

   // ResProxies
   @XmlElement(name = "resProxy-section")
   private final ResProxyReport resProxyReport = new ResProxyReport();

   // XMLPopulatedValidator
   @XmlElement(name = "xml-populated-section")
   private final XmlPopulationReport xmlPopulationReport = new XmlPopulationReport();;

   // XMLValidator
   @XmlElement(name = "xml-validation-section")
   private final XmlValidityReport xmlValidationReport = new XmlValidityReport();

   // URL
   @XmlElement(name = "url-validation-section")
   private final LinkcheckerReport linkcheckerReport = new LinkcheckerReport();

   // Facets
   @XmlElement(name = "facet-section")
   private final FacetReport facetReport = new FacetReport();

   // Invalid Files
   @XmlElementWrapper(name = "invalid-files")
   private final Collection<InvalidFile> invalidFiles = new ArrayList<InvalidFile>();

   @XmlRootElement
   @RequiredArgsConstructor
   public static class InvalidFile {
      @XmlAttribute
      private final String origin;
      @XmlAttribute
      private final String reason;
   }


   @Override
   public String getName() {
      return fileReport.getProvider();
   }
   
   @Override
   public void addReport(CMDInstanceReport instanceReport) {
      
      if(!instanceReport.isValid()) {
         
         this.invalidFiles.add(
               new InvalidFile(instanceReport.getFileReport().getLocation(), 
               instanceReport.getScoring().getMessages().stream().filter(message -> message.getSeverity()==Severity.FATAL).findFirst().get().getIssue()
            ));

         return;
      }
      
      this.score += instanceReport.getScoring().getScore();
      if (instanceReport.getScoring().getScore() > this.insMaxScore) {
          this.insMaxScore = instanceReport.getScoring().getScore();
      }

      if (instanceReport.getScoring().getScore() < this.insMinScore)
          this.insMinScore = instanceReport.getScoring().getScore();

      this.maxPossibleScoreInstance = instanceReport.getScoring().getMaxScore();

      // ResProxies
      this.resProxyReport.addTotNumOfResProxies(instanceReport.getResProxyReport().getNumOfResProxies());
      this.resProxyReport.addTotNumOfResourcesWithMime(instanceReport.getResProxyReport().getNumOfResourcesWithMime());
      this.resProxyReport.addTotNumOfResProxiesWithReferences(instanceReport.getResProxyReport().getNumOfResProxiesWithReferences());

      // XMLPopulatedValidator
      this.xmlPopulationReport.addTotNumOfXMLElements(instanceReport.getXmlPopulationReport().getNumOfXMLElements());
      this.xmlPopulationReport.addTotNumOfXMLSimpleElements(instanceReport.getXmlPopulationReport().getNumOfXMLSimpleElements());
      this.xmlPopulationReport.addTotNumOfXMLEmptyElements(instanceReport.getXmlPopulationReport().getNumOfXMLEmptyElements());




      // Facet
      instanceReport
         .getFacetReport()
         .getCoverages()
         .stream()
         .filter(Coverage::isCoveredByInstance)
         .forEach(facet -> this.facetReport.getFacetMap().get(facet.getName()).incrementCount());


//      this.handleProfile(instanceReport.header.getId(), instanceReport.profileScore);
      
   }

   @Override
   public Scoring newScore() {
      // TODO Auto-generated method stub
      return null;
   }

}

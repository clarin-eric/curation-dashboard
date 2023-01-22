package eu.clarin.cmdi.curation.api.report.collection;

import eu.clarin.cmdi.curation.api.report.Issue;
import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.FacetReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.FileReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.HeaderReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.LinkcheckerReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.ResProxyReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.XmlPopulationReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.XmlValidityReport;
import lombok.NoArgsConstructor;
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
public class CollectionReport implements NamedReport{
   
   @XmlAttribute(name = "score")
   public double score = 0.0;
   @XmlAttribute(name = "avg-score")
   public double avgScore;
   @XmlAttribute(name = "min-score")
   public double insMinScore = Double.MAX_VALUE;
   @XmlAttribute(name = "max-score")
   public double insMaxScore;
   @XmlAttribute(name = "col-max-score")
   public double maxScore;
   @XmlAttribute(name = "score-percentage")
   public double scorePercentage;
   @XmlAttribute(name = "ins-max-score")
   public Double maxPossibleScoreInstance = 0.0;
   @XmlAttribute(name = "creation-time")
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public LocalDateTime creationTime = LocalDateTime.now();

   @XmlElement(name = "file-section")
   public final FileReport fileReport = new FileReport();

   @XmlElement(name = "header-section")
   public final HeaderReport headerReport = new HeaderReport();

   // ResProxies
   @XmlElement(name = "resProxy-section")
   public final ResProxyReport resProxyReport = new ResProxyReport();

   // XMLPopulatedValidator
   @XmlElement(name = "xml-populated-section")
   public final XmlPopulationReport xmlPopulationReport = new XmlPopulationReport();;

   // XMLValidator
   @XmlElement(name = "xml-validation-section")
   public final XmlValidityReport xmlValidationReport = new XmlValidityReport();

   // URL
   @XmlElement(name = "url-validation-section")
   public final LinkcheckerReport linkcheckerReport = new LinkcheckerReport();

   // Facets
   @XmlElement(name = "facet-section")
   public final FacetReport facetReport = new FacetReport();

   // Invalid Files
   @XmlElementWrapper(name = "issues")
   public final Collection<OriginIssue> issues = new ArrayList<OriginIssue>();

   @XmlRootElement
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class OriginIssue {
      @XmlAttribute
      private final String origin;
      @XmlElementWrapper(name = "issues")
      @XmlElement
      private final Collection<Issue> issues;
   }


   @Override
   public String getName() {
      return fileReport.provider;
   }
}

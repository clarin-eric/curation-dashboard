package eu.clarin.cmdi.curation.api.report.instance;

import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.api.report.Scoring;
import eu.clarin.cmdi.curation.api.report.instance.section.FileReport;
import eu.clarin.cmdi.curation.api.report.instance.section.InstanceFacetReport;
import eu.clarin.cmdi.curation.api.report.instance.section.InstanceHeaderReport;
import eu.clarin.cmdi.curation.api.report.instance.section.ResProxyReport;
import eu.clarin.cmdi.curation.api.report.instance.section.XmlPopulationReport;
import eu.clarin.cmdi.curation.api.report.instance.section.XmlValidityReport;
import eu.clarin.cmdi.curation.api.report.profile.section.ProfileHeaderReport;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDateTime;
import java.util.stream.Stream;


/**
 *
 */

@XmlRootElement(name = "instance-report")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@Getter
@Setter
public class CMDInstanceReport extends ScoreReport implements NamedReport {
   

//   @XmlAttribute
//   private double score = 0.0;

   @XmlAttribute(name = "ins-score")
   private double instanceScore = 0.0;

   @XmlAttribute(name = "pfl-score")
   private double profileScore = 0.0;

   @XmlAttribute(name = "max-score")
   private double maxScore;

   @XmlAttribute(name = "score-percentage")
   private double scorePercentage;

   @XmlAttribute(name = "creation-time")
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   private LocalDateTime creationTime = LocalDateTime.now();

   // sub reports **************************************


   // ProfileHeader
   @XmlElement(name = "profile-section")
   private ProfileHeaderReport headerReport;
   //InstanceHeader
   @XmlElement(name = "instance-header-section")
   private InstanceHeaderReport instanceHeaderReport;

   // file
   @XmlElement(name = "file-section")
   private FileReport fileReport;

   // ResProxy
   @XmlElement(name = "resProxy-section")
   public ResProxyReport resProxyReport;

   // XMLPopulatedValidator
   @XmlElement(name = "xml-populated-section")
   private XmlPopulationReport xmlPopulatedReport;

   // XMLValidityValidator
   @XmlElement(name = "xml-validation-section")
   private XmlValidityReport xmlValidityReport;

   // facets
   @XmlElement(name = "facets-section")
   private InstanceFacetReport facetReport;

   @Override
   public String getName() {
      if (fileReport.getLocation() != null && fileReport.getLocation().contains(".xml")) {
         String normalisedPath = fileReport.getLocation().replace('\\', '/');
         return normalisedPath.substring(normalisedPath.lastIndexOf('/') + 1, normalisedPath.lastIndexOf('.'));
      }
      else {
         return fileReport.getLocation();
      }

   }

   @Override
   public boolean isValid() {
      return getSegmentReports().filter(segmentReport -> !segmentReport.isValid()).findFirst().isEmpty();
   }

   @Override
   public Scoring newScore() {
      return new Scoring() {

         @Override
         public double getMaxScore() {
            return getSegmentReports().mapToDouble(scoreReport -> scoreReport.getScore().getMaxScore()).sum();
         }

         @Override
         public double getScore() {
            return getSegmentReports().mapToDouble(scoreReport -> scoreReport.getScore().getScore()).sum();
         }        
      };
   }
   
   private Stream<ScoreReport> getSegmentReports(){
      return Stream.of(
            this.headerReport, 
            this.instanceHeaderReport, 
            this.fileReport, 
            this.resProxyReport, 
            this.xmlPopulatedReport, 
            this.xmlValidityReport, 
            this.facetReport
         ).filter(report -> report != null);
   }
}

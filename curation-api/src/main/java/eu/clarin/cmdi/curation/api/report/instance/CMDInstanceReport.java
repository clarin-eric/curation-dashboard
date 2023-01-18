package eu.clarin.cmdi.curation.api.report.instance;

import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.report.ScoreReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.FileReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceHeaderReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.ResourceProxyReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.XmlPopulationReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.XmlValidityReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileHeaderReport;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDateTime;
import java.util.stream.Stream;


/**
 *
 */

@XmlRootElement(name = "instance-report")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class CMDInstanceReport extends ScoreReport implements NamedReport {
   

//   @XmlAttribute
//   private double score = 0.0;

   @XmlAttribute(name = "ins-score")
   public double instanceScore = 0.0;

   @XmlAttribute(name = "pfl-score")
   public double profileScore = 0.0;

   @XmlAttribute(name = "max-score")
   public double maxScore;

   @XmlAttribute(name = "score-percentage")
   public double scorePercentage;

   @XmlAttribute(name = "creation-time")
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public final LocalDateTime creationTime = LocalDateTime.now();

   // sub reports **************************************


   // ProfileHeader
   @XmlElement(name = "profile-section")
   public ProfileHeaderReport headerReport;
   //InstanceHeader
   @XmlElement(name = "instance-header-section")
   public InstanceHeaderReport instanceHeaderReport;

   // file
   @XmlElement(name = "file-section")
   public FileReport fileReport;

   // ResProxy
   @XmlElement(name = "resProxy-section")
   public ResourceProxyReport resProxyReport;

   // XMLPopulatedValidator
   @XmlElement(name = "xml-populated-section")
   public XmlPopulationReport xmlPopulationReport;

   // XMLValidityValidator
   @XmlElement(name = "xml-validation-section")
   public XmlValidityReport xmlValidityReport;

   // facets
   @XmlElement(name = "facets-section")
   public InstanceFacetReport facetReport;

   @Override
   public String getName() {
      if (fileReport.location != null && fileReport.location.contains(".xml")) {
         String normalisedPath = fileReport.location.replace('\\', '/');
         return normalisedPath.substring(normalisedPath.lastIndexOf('/') + 1, normalisedPath.lastIndexOf('.'));
      }
      else {
         return fileReport.location;
      }

   }

   @Override
   public boolean isValid() {
      return getSegmentReports().filter(segmentReport -> !segmentReport.isValid()).findFirst().isEmpty();
   }

   
   private Stream<ScoreReport> getSegmentReports(){
      return Stream.of(
            this.headerReport, 
            this.instanceHeaderReport, 
            this.fileReport, 
            this.resProxyReport, 
            this.xmlPopulationReport, 
            this.xmlValidityReport, 
            this.facetReport
         ).filter(report -> report != null);
   }
}

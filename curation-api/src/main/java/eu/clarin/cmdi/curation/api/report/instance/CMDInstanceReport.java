package eu.clarin.cmdi.curation.api.report.instance;

import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.*;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileHeaderReport;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;


/**
 *
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class CMDInstanceReport implements NamedReport {
   @XmlTransient
   public boolean isProcessable = true;
   

   @XmlAttribute
   public static final double maxScore = 
      CMDProfileReport.maxScore
      + FileReport.maxScore 
      + InstanceHeaderReport.maxScore 
      + ResourceProxyReport.maxScore 
      + XmlPopulationReport.maxScore 
      + XmlValidityReport.maxScore
      + InstanceFacetReport.maxScore;

   @XmlAttribute
   public double instanceScore = 0.0;

   @XmlAttribute
   public double profileScore = 0.0;

   @XmlAttribute
   public double scorePercentage;

   @XmlAttribute
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public final LocalDateTime creationTime = LocalDateTime.now();

   @XmlAttribute
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public LocalDateTime previousCreationTime;

   // sub reports **************************************

   // ProfileHeader
   @XmlElement
   public ProfileHeaderReport profileHeaderReport;
   
   //InstanceHeader
   @XmlElement
   public InstanceHeaderReport instanceHeaderReport;

   // file
   @XmlElement
   public FileReport fileReport;

   // ResProxy
   @XmlElement
   public ResourceProxyReport resProxyReport;

   // XMLPopulatedValidator
   @XmlElement
   public XmlPopulationReport xmlPopulationReport;

   // XMLValidityValidator
   @XmlElement
   public XmlValidityReport xmlValidityReport;

   // facets
   @XmlElement
   public InstanceFacetReport facetReport;
   @XmlElementWrapper(name = "details")
   @XmlElement(name = "detail")
   public Collection<Detail> details = new ArrayList<Detail>();

   @Override
   public String getName() {
      if (fileReport.location != null && fileReport.location.contains(".xml")) {
         String normalisedPath = fileReport.location.replace('\\', '/');
         return FileNameEncoder.encode(normalisedPath);
      }
      else {
         return fileReport.location;
      }
   }

   @Override
   public LocalDateTime getCreationTime() {

      return this.creationTime;
   }

   @Override
   public void setPreviousCreationTime(LocalDateTime previousCreationTime) {

      this.previousCreationTime = previousCreationTime;
   }

   @Override
   public LocalDateTime getPreviousCreationTime() {

      return this.previousCreationTime;
   }
}

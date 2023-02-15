package eu.clarin.cmdi.curation.api.report.instance;

import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.NamedReport;

import eu.clarin.cmdi.curation.api.report.instance.sec.FileReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.InstanceHeaderReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.ResourceProxyReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.XmlPopulationReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.XmlValidityReport;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileHeaderReport;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
   @XmlElement
   public Collection<Detail> details = new ArrayList<Detail>();

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
}

package eu.clarin.cmdi.curation.api.report.instance;

import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.Issue;
import eu.clarin.cmdi.curation.api.report.NamedReport;

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
import java.util.ArrayList;
import java.util.Collection;


/**
 *
 */

@XmlRootElement(name = "instance-report")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class CMDInstanceReport implements NamedReport {
   @XmlTransient
   public boolean isValidReport = true;

   @XmlAttribute(name = "ins-score")
   public double instanceScore = 0.0;

   @XmlAttribute(name = "pfl-score")
   public double profileScore = 0.0;

   @XmlAttribute(name = "max-score")
   public final double maxScore = 15.0;

   @XmlAttribute(name = "score-percentage")
   public double scorePercentage;

   @XmlAttribute(name = "creation-time")
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public final LocalDateTime creationTime = LocalDateTime.now();

   // sub reports **************************************


   // ProfileHeader
   @XmlElement(name = "profile-section")
   public ProfileHeaderReport profileHeaderReport;
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
   @XmlElement
   public Collection<Issue> issues = new ArrayList<Issue>();

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

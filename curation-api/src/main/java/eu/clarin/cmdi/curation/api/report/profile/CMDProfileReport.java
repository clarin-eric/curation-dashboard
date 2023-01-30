package eu.clarin.cmdi.curation.api.report.profile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ComponentReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ConceptReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileFacetReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileHeaderReport;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * A selection of values from a single CMDProfileReport which will form a line
 * in a statistical overview
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDProfileReport implements NamedReport{
   @XmlTransient
   public boolean isValidReport = true;
   @XmlAttribute
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public final LocalDateTime creationTime = LocalDateTime.now();
   @XmlAttribute
   public static final double maxScore = 3.0;
   @XmlAttribute
   public double score;

   @XmlElement
   public ProfileHeaderReport headerReport;
   @XmlElement
   public ComponentReport componentReport;
   @XmlElement
   public ConceptReport conceptReport;
   @XmlElement
   public ProfileFacetReport facetReport;
   
   

   @XmlElementWrapper(name = "collectionUsage")
   @XmlElement(name = "collection")
   public final Collection<CollectionUsage> collectionUsage = new ArrayList<CollectionUsage>();
   @XmlElementWrapper(name = "issues")
   @XmlElement(name = "issue")
   public final Collection<Detail> issues = new ArrayList<Detail>();


   @Override
   public String getName() {
      return (this.headerReport!=null?this.headerReport.getId():"missing header report");
   }



   @XmlRootElement
   @XmlAccessorType(XmlAccessType.FIELD)
   @RequiredArgsConstructor
   @NoArgsConstructor(force = true)
   public static class CollectionUsage {
      @XmlAttribute
      public final String collectionName;
      @XmlAttribute
      public int count = 1;
   }
}

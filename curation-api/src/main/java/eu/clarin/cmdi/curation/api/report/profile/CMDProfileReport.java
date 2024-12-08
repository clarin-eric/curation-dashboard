package eu.clarin.cmdi.curation.api.report.profile;

import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ComponentReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ConceptReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileFacetReport;
import eu.clarin.cmdi.curation.api.report.profile.sec.ProfileHeaderReport;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A selection of values from a single CMDProfileReport which will form a line
 * in a statistical overview
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDProfileReport implements NamedReport {
   @XmlTransient
   public boolean isValidReport = true;
   @XmlAttribute
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public final LocalDateTime creationTime = LocalDateTime.now();

   @XmlAttribute
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public LocalDateTime previousCreationTime;
   @XmlAttribute
   public static final double maxScore = 4.0;
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
   @XmlElementWrapper(name = "details")
   @XmlElement(name = "detail")
   public final Collection<Detail> details = new ArrayList<Detail>();


   @Override
   @XmlTransient
   public String getName() {

      return FileNameEncoder.encode(this.headerReport.getSchemaLocation());
   }

   @Override
   @XmlTransient
   public LocalDateTime getCreationTime() {

      return this.creationTime;
   }

   @Override
   public void setPreviousCreationTime(LocalDateTime previousCreationTime) {

      this.previousCreationTime = previousCreationTime;
   }

   @Override
   @XmlTransient
   public LocalDateTime getPreviousCreationTime() {

      return this.previousCreationTime;
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

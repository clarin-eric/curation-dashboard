package eu.clarin.cmdi.curation.api.report.linkchecker;

import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.LinkcheckerReport.Statistics;
import eu.clarin.linkchecker.persistence.utils.Category;
import jakarta.xml.bind.annotation.*;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class AllLinkcheckerReport implements NamedReport {
   @XmlAttribute
   @XmlJavaTypeAdapter(LocalDateTimeAdapter.class)
   public LocalDateTime creationTime = LocalDateTime.now();

   @XmlElement(name = "overall")
   private Overall overall = new Overall();

   @XmlElement(name = "collection")
   private Set<CMDCollection> collections = new TreeSet<CMDCollection>((col1, col2) -> col1.name.compareTo(col2.name));

   @Override
   public String getName() {
      
      return getClass().getSimpleName();
   
   }

   public void addReport(CollectionReport collectionReport) {

      this.collections.add(new CMDCollection((CollectionReport) collectionReport, this));

   }

   public static class CMDCollection {
      @XmlAttribute
      private String name;

      @XmlAttribute
      private int totNumOfLinksWithDuration;

      @XmlAttribute
      private Double avgRespTime;

      @XmlAttribute
      private Long maxRespTime;

      @XmlElement
      private Collection<Statistics> statistics;

      public CMDCollection() {

      }

      public CMDCollection(CollectionReport collectionReport, AllLinkcheckerReport linkcheckerReport) {
         this.name = collectionReport.getName();
         this.statistics = collectionReport.linkcheckerReport.statistics;
         this.totNumOfLinksWithDuration = collectionReport.linkcheckerReport.totNumOfLinksWithDuration;
         this.avgRespTime = collectionReport.linkcheckerReport.avgRespTime;
         this.maxRespTime = collectionReport.linkcheckerReport.maxRespTime;
         
         if(linkcheckerReport.overall.avgRespTime == null) {
            
            linkcheckerReport.overall.avgRespTime = collectionReport.linkcheckerReport.avgRespTime;
         }
         else if(collectionReport.linkcheckerReport.avgRespTime != null) {
            linkcheckerReport.overall.avgRespTime = (linkcheckerReport.overall.avgRespTime
                  * linkcheckerReport.overall.totNumOfLinksWithDuration
                  + collectionReport.linkcheckerReport.avgRespTime * collectionReport.linkcheckerReport.totNumOfLinksWithDuration)
                  / (linkcheckerReport.overall.totNumOfLinksWithDuration + collectionReport.linkcheckerReport.totNumOfLinksWithDuration);
         }

         linkcheckerReport.overall.totNumOfLinksWithDuration += collectionReport.linkcheckerReport.totNumOfLinksWithDuration;
         if (
               linkcheckerReport.overall.maxRespTime == null || 
               (collectionReport.linkcheckerReport.maxRespTime != null && linkcheckerReport.overall.maxRespTime < collectionReport.linkcheckerReport.maxRespTime)
            ) 
         {
            linkcheckerReport.overall.maxRespTime = collectionReport.linkcheckerReport.maxRespTime;
         }
         
         collectionReport.linkcheckerReport.statistics.forEach(linkcheckerReport.overall::addStatistics);
      }
   }

   public static class Overall {
      private TreeMap<Category, Statistics> statistics;
      @XmlAttribute
      private int totNumOfLinksWithDuration;

      @XmlAttribute
      private Double avgRespTime;

      @XmlAttribute
      private Long maxRespTime;

      @XmlElement(name = "statistics")
      public Collection<Statistics> getStatistics() {
         return this.statistics.values();
      }

      public Overall() {
         this.statistics = new TreeMap<Category, Statistics>();
      }
      
      public void addStatistics(Statistics additionalStatistics) {
         
         Statistics statistics = this.statistics
            .computeIfAbsent(additionalStatistics.category, k -> new Statistics(k));

            
         if(additionalStatistics.avgRespTime != null) {
            if(statistics.avgRespTime != null) {
               statistics.avgRespTime =
                     (statistics.avgRespTime * statistics.nonNullCount + additionalStatistics.avgRespTime * additionalStatistics.count)
                     / (statistics.nonNullCount + additionalStatistics.count);
            }
            else {
               statistics.avgRespTime = additionalStatistics.avgRespTime;
            }
            statistics.nonNullCount += additionalStatistics.count;
         }
         statistics.count += additionalStatistics.count;
         
         if(statistics.maxRespTime == null || (additionalStatistics.maxRespTime != null && statistics.maxRespTime < additionalStatistics.maxRespTime)) {
            statistics.maxRespTime = additionalStatistics.maxRespTime;
         }
      }
   }
}

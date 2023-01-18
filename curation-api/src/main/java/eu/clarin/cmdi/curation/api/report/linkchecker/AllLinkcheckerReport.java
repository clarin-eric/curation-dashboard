package eu.clarin.cmdi.curation.api.report.linkchecker;

import eu.clarin.cmdi.curation.api.report.LocalDateTimeAdapter;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.LinkcheckerReport.Statistics;
import eu.clarin.linkchecker.persistence.utils.Category;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@XmlRootElement(name = "linkchecker-report")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class AllLinkcheckerReport implements NamedReport {
   @XmlAttribute(name = "creation-time")
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
      private int count;

      @XmlAttribute
      private double avgRespTime;

      @XmlAttribute
      private double maxRespTime;

      @XmlElement
      private Collection<Statistics> statistics;

      public CMDCollection() {

      }

      public CMDCollection(CollectionReport collectionReport, AllLinkcheckerReport linkcheckerReport) {
         this.name = collectionReport.getName();
         this.statistics = collectionReport.linkcheckerReport.statistics;
         this.count = collectionReport.linkcheckerReport.totNumOfCheckedLinks;
         this.avgRespTime = collectionReport.linkcheckerReport.avgRespTime;
         this.maxRespTime = collectionReport.linkcheckerReport.maxRespTime;
         
         linkcheckerReport.overall.avgRespTime = (linkcheckerReport.overall.avgRespTime
               * linkcheckerReport.overall.count
               + collectionReport.linkcheckerReport.avgRespTime * collectionReport.linkcheckerReport.totNumOfCheckedLinks)
               / (linkcheckerReport.overall.count + collectionReport.linkcheckerReport.totNumOfCheckedLinks);

         linkcheckerReport.overall.count += collectionReport.linkcheckerReport.totNumOfCheckedLinks;
         if (linkcheckerReport.overall.maxRespTime < collectionReport.linkcheckerReport.maxRespTime) {
            linkcheckerReport.overall.maxRespTime = collectionReport.linkcheckerReport.maxRespTime;
         }
         
         collectionReport.linkcheckerReport.statistics.forEach(linkcheckerReport.overall::addStatistics);
      }
   }

   public static class Overall {
      private TreeMap<Category, Statistics> statistics;
      @XmlAttribute
      private int count;

      @XmlAttribute
      private double avgRespTime;

      @XmlAttribute
      private long maxRespTime;

      @XmlElement(name = "statistics")
      public Collection<Statistics> getStatistics() {
         return this.statistics.values();
      }

      public Overall() {
         this.statistics = new TreeMap<Category, Statistics>();
      }
      
      public void addStatistics(Statistics statisticsObj) {
         if(this.statistics.containsKey(statisticsObj.category)) {
            Statistics s = this.statistics.get(statisticsObj.category);
            
            if(statisticsObj.avgRespTime > 0) {
               s.avgRespTime =
                     (s.avgRespTime * s.nonNullCount + statisticsObj.avgRespTime * statisticsObj.count)
                     / (s.nonNullCount + statisticsObj.count);
               s.nonNullCount += statisticsObj.count;
            }
            s.count += statisticsObj.count;
            
            if(s.maxRespTime < statisticsObj.maxRespTime) {
               s.maxRespTime = statisticsObj.maxRespTime;
            }
         }
         else {
            this.statistics.put(statisticsObj.category, new Statistics() {{
                  this.category = statisticsObj.category;
                  this.count = statisticsObj.count;
                  this.avgRespTime = statisticsObj.avgRespTime;
                  this.maxRespTime = statisticsObj.maxRespTime;
               }});
         }
      }
   }
}

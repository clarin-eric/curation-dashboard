package eu.clarin.cmdi.curation.report;

import eu.clarin.cmdi.curation.report.CollectionReport.Statistics;
import eu.clarin.cmdi.curation.utils.CategoryColor;
import eu.clarin.cmdi.curation.utils.TimeUtils;
import eu.clarin.cmdi.curation.xml.XMLMarshaller;
import eu.clarin.cmdi.rasa.helpers.statusCodeMapper.Category;

import javax.xml.bind.annotation.*;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@XmlRootElement(name = "linkchecker-report")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class LinkCheckerReport implements Report<LinkCheckerReport> {
   @XmlAttribute(name = "creation-time")
   public String creationTime = TimeUtils.humanizeToDate(System.currentTimeMillis());

   @XmlElement(name = "overall")
   private Overall overall = new Overall();

   @XmlElement(name = "collection")
   private List<CMDCollection> collections = new ArrayList<CMDCollection>();

   @Override
   public void setParentName(String parentName) {
   }

   @Override
   public String getParentName() {
      return null;
   }

   @Override
   public String getName() {
      return getClass().getSimpleName();
   }

   @Override
   public boolean isValid() {
      return false;
   }

   @Override
   public void addSegmentScore(Score segmentScore) {
   }

   @Override
   public void toXML(OutputStream os) {
      XMLMarshaller<LinkCheckerReport> instanceMarshaller = new XMLMarshaller<>(LinkCheckerReport.class);
      instanceMarshaller.marshal(this, os);
   }

   @Override
   public void mergeWithParent(LinkCheckerReport parentReport) {
   }

   public void addReport(Report<?> report) {
      if (report instanceof CollectionReport) {
         this.collections.add(new CMDCollection((CollectionReport) report));
      }
   }

   public class CMDCollection {
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

      public CMDCollection(CollectionReport report) {
         this.name = report.getName();
         this.statistics = report.urlReport.category;
         this.count = report.urlReport.totNumOfCheckedLinks;
         this.avgRespTime = report.urlReport.avgRespTime;
         this.maxRespTime = report.urlReport.maxRespTime;
         
         LinkCheckerReport.this.overall.avgRespTime = (LinkCheckerReport.this.overall.avgRespTime
               * LinkCheckerReport.this.overall.count
               + report.urlReport.avgRespTime * report.urlReport.totNumOfCheckedLinks)
               / (LinkCheckerReport.this.overall.count + report.urlReport.totNumOfCheckedLinks);

         LinkCheckerReport.this.overall.count += report.urlReport.totNumOfCheckedLinks;
         if (LinkCheckerReport.this.overall.maxRespTime < report.urlReport.maxRespTime) {
            LinkCheckerReport.this.overall.maxRespTime = report.urlReport.maxRespTime;
         }
         
         report.urlReport.category.forEach(LinkCheckerReport.this.overall::addStatistics);
      }
   }

   public class Overall {
      private List<Statistics> statistics;
      @XmlAttribute
      private int count;

      @XmlAttribute
      private double avgRespTime;

      @XmlAttribute
      private long maxRespTime;

      @XmlElement
      public Collection<Statistics> getStatistics() {
         return this.statistics;
      }

      public Overall() {
         this.statistics = List.of(new Statistics(Category.Broken.name(), CategoryColor.getColor(Category.Broken)),
               new Statistics(Category.Ok.name(), CategoryColor.getColor(Category.Ok)),
               new Statistics(Category.Undetermined.name(), CategoryColor.getColor(Category.Undetermined)));
      }
      
      public void addStatistics(Statistics statisticsObj) {
         this.statistics.stream()
         .filter(s -> s.category.equals(statisticsObj.category))
         .findFirst()
         .ifPresent(s -> {
            s.avgRespTime =
               (s.avgRespTime * s.count + statisticsObj.avgRespTime * statisticsObj.count)
               / (s.count + statisticsObj.count);
            s.count += statisticsObj.count;
            if(s.maxRespTime < statisticsObj.maxRespTime) {
               s.maxRespTime = statisticsObj.maxRespTime;
            }
         });
      }
   }
}

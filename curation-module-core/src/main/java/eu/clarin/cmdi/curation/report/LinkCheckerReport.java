package eu.clarin.cmdi.curation.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.report.CollectionReport.Statistics;

/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
@XmlRootElement(name = "linkchecker-report")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class LinkCheckerReport implements Report<LinkCheckerReport> {
    @XmlElement(name="overall")
    private Overall overall = new Overall();
    
    @XmlElement(name="collection")
    private List<CMDCollection> collections = new ArrayList<CMDCollection>();    

      


    @Override
    public void setParentName(String parentName) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public String getParentName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return getClass().getSimpleName();
    }

    @Override
    public boolean isValid() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void addSegmentScore(Score segmentScore) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void mergeWithParent(LinkCheckerReport parentReport) {
        // TODO Auto-generated method stub
        
    }
    
    public void addReport(Report<?> report) {
        if(report instanceof CollectionReport) {
            
            for(Statistics statistics : ((CollectionReport) report).urlReport.status){
                
                Statistics aggregatedStatistics = this.overall.statisticsMap.get(statistics.statusCode);
                
                if(aggregatedStatistics == null) {
                    aggregatedStatistics = new Statistics();
                    aggregatedStatistics.statusCode = statistics.statusCode;
                    aggregatedStatistics.category = statistics.category;
                    aggregatedStatistics.count = statistics.count;
                    aggregatedStatistics.avgRespTime = statistics.avgRespTime;
                    aggregatedStatistics.maxRespTime = statistics.maxRespTime;
                    
                    this.overall.statisticsMap.put(aggregatedStatistics.statusCode, aggregatedStatistics);
                    
                    this.overall.avgRespTime = statistics.avgRespTime;
                    this.overall.count = statistics.count;
                }
                else {
                    aggregatedStatistics.avgRespTime = ((aggregatedStatistics.avgRespTime * aggregatedStatistics.count) + 
                            (statistics.avgRespTime * statistics.count)) / (aggregatedStatistics.count + statistics.count);
                    aggregatedStatistics.count = aggregatedStatistics.count + statistics.count;
                    aggregatedStatistics.maxRespTime = Math.max(aggregatedStatistics.maxRespTime, statistics.maxRespTime);
                    
                    this.overall.avgRespTime = ((this.overall.avgRespTime * this.overall.count) + 
                            (statistics.avgRespTime * statistics.count)) / (this.overall.count + statistics.count);

                    this.overall.count += statistics.count;
                }
                
            };
                        
            this.collections.add(new CMDCollection((CollectionReport) report));
       
        }
    }
    
    public static class CMDCollection{
        @XmlAttribute
        private String name;
        
        
        @XmlElement
        private Collection<Statistics> statistics;
        
        public CMDCollection() {
            
        }
        public CMDCollection(CollectionReport report) {
            
            this.name = report.getName();
            this.statistics = report.urlReport.status;
        }
    } 
    
   public static class Overall{
       private Map<Integer, Statistics> statisticsMap = new HashMap<Integer, Statistics>();
       @XmlAttribute
       private int count;
       
       @XmlAttribute
       private double avgRespTime;
       
       @XmlElement
       public Collection<Statistics> getStatistics() {
           
           return this.statisticsMap.values();
           
       }
   }
}

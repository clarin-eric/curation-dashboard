package eu.clarin.cmdi.curation.report;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CollectionReport.Statistics;
import eu.clarin.cmdi.curation.xml.XMLMarshaller;

import javax.xml.bind.annotation.*;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
 * @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 */
@XmlRootElement(name = "linkchecker-report")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class LinkCheckerReport implements Report<LinkCheckerReport> {
    @XmlElement(name = "overall")
    private Overall overall = new Overall();

    @XmlElement(name = "collection")
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
    public void toXML(OutputStream os) {
        XMLMarshaller<LinkCheckerReport> instanceMarshaller = new
                XMLMarshaller<>(LinkCheckerReport.class);
        instanceMarshaller.marshal(this, os);
    }

    @Override
    public void mergeWithParent(LinkCheckerReport parentReport) {
        // TODO Auto-generated method stub

    }

    public void addReport(Report<?> report) {

        if (report instanceof CollectionReport) {


//            for (Statistics statistics : ((CollectionReport) report).urlReport.status) {
//
//                Statistics aggregatedStatistics = this.overall.statisticsMap.get(statistics.statusCode);
//
//                if (aggregatedStatistics == null) {
//                    aggregatedStatistics = new Statistics();
//                    aggregatedStatistics.statusCode = statistics.statusCode;
//                    aggregatedStatistics.category = statistics.category;
//                    aggregatedStatistics.count = statistics.count;
//                    aggregatedStatistics.avgRespTime = statistics.avgRespTime;
//                    aggregatedStatistics.maxRespTime = statistics.maxRespTime;
//
//                    this.overall.statisticsMap.put(aggregatedStatistics.statusCode, aggregatedStatistics);
//
//                    this.overall.avgRespTime = statistics.avgRespTime;
//                    this.overall.count = statistics.count;
//                } else {
//                    aggregatedStatistics.avgRespTime = ((aggregatedStatistics.avgRespTime * aggregatedStatistics.count) +
//                            (statistics.avgRespTime * statistics.count)) / (aggregatedStatistics.count + statistics.count);
//                    aggregatedStatistics.count = aggregatedStatistics.count + statistics.count;
//                    aggregatedStatistics.maxRespTime = Math.max(aggregatedStatistics.maxRespTime, statistics.maxRespTime);
//
//                    this.overall.avgRespTime = ((this.overall.avgRespTime * this.overall.count) +
//                            (statistics.avgRespTime * statistics.count)) / (this.overall.count + statistics.count);
//
//                    this.overall.count += statistics.count;
//                }
//
//            }

            this.collections.add(new CMDCollection((CollectionReport) report));

        }
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

        public CMDCollection(CollectionReport report) {

            this.name = report.getName();
            this.statistics = report.urlReport.status;
            this.count = report.urlReport.totNumOfCheckedLinks;
            this.avgRespTime = report.urlReport.avgRespTime;
            this.maxRespTime = report.urlReport.maxRespTime;
        }


    }

    public static class Overall {
        private List<Statistics> statistics = new ArrayList<>();
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


            List<eu.clarin.cmdi.rasa.links.Statistics> stats = Configuration.statisticsResource.getStatusStatistics("Overall");

            for (eu.clarin.cmdi.rasa.links.Statistics statistics : stats) {
                Statistics xmlStatistics = new Statistics();
                xmlStatistics.avgRespTime = statistics.getAvgRespTime();
                xmlStatistics.maxRespTime = statistics.getMaxRespTime();
                xmlStatistics.statusCode = statistics.getStatus();
                xmlStatistics.category = statistics.getCategory();
                xmlStatistics.count = statistics.getCount();
                this.statistics.add(xmlStatistics);
            }

            eu.clarin.cmdi.rasa.links.Statistics statistics = Configuration.statisticsResource.getOverallStatistics("Overall");
            this.avgRespTime = statistics.getAvgRespTime();
            this.count = statistics.getCount();
            this.maxRespTime = statistics.getMaxRespTime();
        }
    }
}

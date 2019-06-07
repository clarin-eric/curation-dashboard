package eu.clarin.cmdi.curation.report;

import java.io.OutputStream;
import java.util.*;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CollectionReport.Statistics;
import eu.clarin.cmdi.curation.xml.XMLMarshaller;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

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
            MongoClient mongoClient;
            if (Configuration.DATABASE_URI == null || Configuration.DATABASE_URI.isEmpty()) {//if it is empty, try localhost
                mongoClient = MongoClients.create();
            } else {
                mongoClient = MongoClients.create(Configuration.DATABASE_URI);
            }

            MongoDatabase database = mongoClient.getDatabase(Configuration.DATABASE_NAME);

            MongoCollection<Document> linksChecked = database.getCollection("linksChecked");

            AggregateIterable<Document> iterable = linksChecked.aggregate(Arrays.asList(
                    Aggregates.group("$status",
                            Accumulators.sum("count", 1),
                            Accumulators.avg("avg_resp", "$duration"),
                            Accumulators.max("max_resp", "$duration")
                    ),
                    Aggregates.sort(orderBy(ascending("_id")))
            ));

            for (Document doc : iterable) {
                Statistics statistics = new Statistics();
                statistics.avgRespTime = doc.getDouble("avg_resp");
                statistics.maxRespTime = doc.getLong("max_resp");
                statistics.statusCode = doc.getInteger("_id");
                if (statistics.statusCode == 200) {
                    statistics.category = "Ok";
                } else if (statistics.statusCode == 401 || statistics.statusCode == 405 || statistics.statusCode == 429) {
                    statistics.category = "Undetermined";
                } else {
                    statistics.category = "Broken";
                }
                statistics.count = doc.getInteger("count");

                this.statistics.add(statistics);
            }

            AggregateIterable<Document> aggregate = linksChecked.aggregate(
                    Arrays.asList(
                            Aggregates.group(null,
                                    Accumulators.avg("avg_resp", "$duration"),
                                    Accumulators.max("max_resp", "$duration"),
                                    Accumulators.sum("count", 1)
                            )));
            Document result = aggregate.first();
            this.avgRespTime = result.getDouble("avg_resp");
            this.maxRespTime = result.getLong("max_resp");
            this.count = result.getInteger("count");
        }
    }
}

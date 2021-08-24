package eu.clarin.cmdi.curation.report;

import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CollectionReport.Statistics;
import eu.clarin.cmdi.curation.utils.CategoryColor;
import eu.clarin.cmdi.curation.xml.XMLMarshaller;
import eu.clarin.cmdi.rasa.DAO.Statistics.CategoryStatistics;
import eu.clarin.cmdi.rasa.filters.CheckedLinkFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.*;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@XmlRootElement(name = "linkchecker-report")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class LinkCheckerReport implements Report<LinkCheckerReport> {

    @XmlElement(name = "overall")
    private Overall overall = null;

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
        XMLMarshaller<LinkCheckerReport> instanceMarshaller = new
                XMLMarshaller<>(LinkCheckerReport.class);
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
    
    public void createOverall() {
    	this.overall = new Overall();
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
            this.statistics = report.urlReport.category;
            this.count = report.urlReport.totNumOfCheckedLinks;
            this.avgRespTime = report.urlReport.avgRespTime;
            this.maxRespTime = report.urlReport.maxRespTime;
        }


    }

    public static class Overall {
        private static final Logger LOG = LoggerFactory.getLogger(Overall.class);
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
        	CheckedLinkFilter filter = Configuration.checkedLinkResource.getCheckedLinkFilter().setProviderGroupIs("Overall").setIsActive(true);
        	
            try(Stream<CategoryStatistics> stream = Configuration.checkedLinkResource.getCategoryStatistics(filter)) {
            	
            	
                stream.forEach(statistics -> {
	                    Statistics xmlStatistics = new Statistics();
	                    xmlStatistics.avgRespTime = statistics.getAvgRespTime();
	                    xmlStatistics.maxRespTime = statistics.getMaxRespTime();
	                    xmlStatistics.category = statistics.getCategory().name();
	                    xmlStatistics.count = statistics.getCount();
	                    xmlStatistics.colorCode = CategoryColor.getColor(statistics.getCategory());
	                    this.statistics.add(xmlStatistics);
	                });
            }
            catch(Exception ex) {
            	LOG.error("There was a problem getting the overall category statistics: " + ex.getMessage());
            }
            try {

                eu.clarin.cmdi.rasa.DAO.Statistics.Statistics statistics = Configuration.checkedLinkResource.getStatistics(filter);
                this.avgRespTime = statistics.getAvgRespTime();
                this.count = (int) statistics.getCount();
                this.maxRespTime = statistics.getMaxRespTime();

            } catch (SQLException ex) {
                LOG.error("There was a problem getting the overall statistics: " + ex.getMessage());
                this.avgRespTime = 0;
                this.count = 0;
                this.maxRespTime = 0;
            }
        }
    }
}

package eu.clarin.cmdi.curation.report;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.*;

import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.utils.TimeUtils;
import eu.clarin.cmdi.curation.xml.XMLMarshaller;
import org.bson.Document;
import org.bson.conversions.Bson;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Sorts.ascending;
import static com.mongodb.client.model.Sorts.orderBy;

/**
 * @author dostojic
 */

@XmlRootElement(name = "collection-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionReport implements Report<CollectionReport> {

    @XmlAttribute(name = "score")
    public Double score = 0.0;

    @XmlAttribute(name = "avg-score")
    public Double avgScore = 0.0;

    @XmlAttribute(name = "min-score")
    public Double insMinScore = Double.MAX_VALUE;

    @XmlAttribute(name = "max-score")
    public Double insMaxScore = 0.0;

    @XmlAttribute(name = "col-max-score")
    public Double maxScore = 0.0;

    @XmlAttribute(name = "score-percentage")
    public Double scorePercentage;

    @XmlAttribute(name = "ins-max-score")
    public Double maxPossibleScoreInstance = 0.0;

    @XmlAttribute
    public String timeStamp = TimeUtils.humanizeToDate(System.currentTimeMillis());

    @XmlElement(name = "file-section")
    public FileReport fileReport;

    @XmlElement(name = "header-section")
    public HeaderReport headerReport;

    // ResProxies
    @XmlElement(name = "resProxy-section")
    public ResProxyReport resProxyReport;

    // XMLPopulatedValidator
    @XmlElement(name = "xml-populated-section")
    public XMLPopulatedReport xmlPopulatedReport;

    // XMLValidator
    @XmlElement(name = "xml-validation-section")
    public XMLValidationReport xmlValidationReport;

    // URL
    @XmlElement(name = "url-validation-section")
    public URLValidationReport urlReport;

    // Facets
    @XmlElement(name = "facet-section")
    public FacetReport facetReport;

    // Invalid Files
    @XmlElementWrapper(name = "invalid-files")
    public Collection<InvalidFile> file;

    @XmlRootElement
    public static class InvalidFile {
        @XmlValue
        public String recordName;

        @XmlAttribute(name = "reason")
        public String reason;
    }

    public void addInvalidFile(InvalidFile invalidFile) {
        if (this.file == null) {
            this.file = new ArrayList<>();
        }
        this.file.add(invalidFile);
    }

    // URLs
    @XmlElementWrapper(name = "single-url-report")
    public Collection<CMDInstanceReport.URLElement> url;

    @XmlRootElement
    public static class URLElement {
        @XmlValue
        public String url;

        @XmlAttribute(name = "method")
        public String method;

        @XmlAttribute(name = "message")
        public String message;

        @XmlAttribute(name = "http-status")
        public int status;

        @XmlAttribute(name = "content-type")
        public String contentType;

        @XmlAttribute(name = "byte-size")
        public String byteSize;

        @XmlAttribute(name = "request-duration")
        public String duration;//either duration in milliseconds or 'timeout'

        @XmlAttribute(name = "timestamp")
        public String timestamp;

        @XmlAttribute(name = "redirectCount")
        public int redirectCount;
    }

    public void addURLElement(CMDInstanceReport.URLElement urlElement) {
        if (this.url == null) {
            this.url = new ArrayList<>();
        }
        this.url.add(urlElement);
    }

    public void handleProfile(String profile, double score) {
        if (headerReport == null)
            headerReport = new HeaderReport();
        if (headerReport.profiles == null)
            headerReport.profiles = new Profiles();
        headerReport.profiles.handleProfile(profile, score);
    }

    public void handleProfile(Profile profile) {
        if (headerReport.profiles == null)
            headerReport.profiles = new Profiles();
        headerReport.profiles.handleProfile(profile);
    }

    @Override
    public String getName() {
        return fileReport.provider;
    }

    @Override
    public void setParentName(String parentName) {
        //don't do anything, there is no parent to a collection
    }

    @Override
    public String getParentName() {
        return null;
    }


    @Override
    public void mergeWithParent(CollectionReport parentReport) {

        parentReport.score += score;
        if (insMinScore < parentReport.insMinScore)
            parentReport.insMinScore = insMinScore;

        if (insMaxScore > parentReport.insMaxScore)
            parentReport.insMaxScore = insMaxScore;

        // ResProxies

        parentReport.resProxyReport.totNumOfResProxies += resProxyReport.totNumOfResProxies;
        parentReport.resProxyReport.totNumOfResourcesWithMime += resProxyReport.totNumOfResourcesWithMime;
        parentReport.resProxyReport.totNumOfResProxiesWithReferences += resProxyReport.totNumOfResProxiesWithReferences;

        // XMLValidator
        parentReport.xmlValidationReport.totNumOfRecords += xmlValidationReport.totNumOfRecords;
        parentReport.xmlValidationReport.totNumOfValidRecords += xmlValidationReport.totNumOfValidRecords;
        parentReport.xmlValidationReport.record.addAll(this.xmlValidationReport.record);

        // XMLPopulatedValidator
        parentReport.xmlPopulatedReport.totNumOfXMLElements += xmlPopulatedReport.totNumOfXMLElements;
        parentReport.xmlPopulatedReport.totNumOfXMLSimpleElements += xmlPopulatedReport.totNumOfXMLSimpleElements;
        parentReport.xmlPopulatedReport.totNumOfXMLEmptyElement += xmlPopulatedReport.totNumOfXMLEmptyElement;

        // URL
        parentReport.urlReport.totNumOfLinks += urlReport.totNumOfLinks;
//        parentReport.urlReport.totNumOfUniqueLinks += urlReport.totNumOfUniqueLinks;
        parentReport.urlReport.totNumOfCheckedLinks += urlReport.totNumOfCheckedLinks;
        parentReport.urlReport.totNumOfResProxiesLinks += urlReport.totNumOfResProxiesLinks;
        parentReport.urlReport.totNumOfBrokenLinks += urlReport.totNumOfBrokenLinks;

        // Facet
        facetReport.facet.forEach(facet -> {
            FacetCollectionStruct parFacet = parentReport.facetReport.facet.stream().filter(f -> f.name.equals(facet.name)).findFirst().orElse(null);
            parFacet.cnt += facet.cnt;
        });

        // Profiles
        for (Profile p : headerReport.profiles.profiles)
            parentReport.handleProfile(p);

        // MDSelfLinks
        if (headerReport.duplicatedMDSelfLink != null && !headerReport.duplicatedMDSelfLink.isEmpty()) {

            if (parentReport.headerReport.duplicatedMDSelfLink == null) {
                parentReport.headerReport.duplicatedMDSelfLink = new ArrayList<>();
            }

            for (String mdSelfLink : headerReport.duplicatedMDSelfLink)
                if (!parentReport.headerReport.duplicatedMDSelfLink.contains(mdSelfLink))
                    parentReport.headerReport.duplicatedMDSelfLink.add(mdSelfLink);
        }

        // invalid files
        if (this.file != null) {
            if (parentReport.file == null)
                parentReport.file = new ArrayList<>();
            parentReport.file.addAll(this.file);
        }

        // urls
        if (this.url != null) {
            if (parentReport.url == null)
                parentReport.url = new ArrayList<>();
            parentReport.url.addAll(this.url);
        }

    }

    @Override
    public void addSegmentScore(Score segmentScore) {
        //not used
    }


    @Override
    public boolean isValid() {
        return true;
    }

    public void calculateAverageValues() {

        // file
        fileReport.avgSize = fileReport.size / fileReport.numOfFiles;

        // ResProxies
        resProxyReport.avgNumOfResProxies = (double) resProxyReport.totNumOfResProxies / fileReport.numOfFiles;
        resProxyReport.avgNumOfResourcesWithMime = (double) resProxyReport.totNumOfResourcesWithMime
                / fileReport.numOfFiles;
        resProxyReport.avgNumOfResProxiesWithReferences = (double) resProxyReport.totNumOfResProxiesWithReferences
                / fileReport.numOfFiles;

        // XMLValidator
        xmlValidationReport.ratioOfValidRecords = xmlValidationReport.totNumOfRecords == 0 ? 0 : (double) xmlValidationReport.totNumOfValidRecords / xmlValidationReport.totNumOfRecords;

        // XMLPopulatedValidator
        xmlPopulatedReport.avgNumOfXMLElements = (double) xmlPopulatedReport.totNumOfXMLElements / fileReport.numOfFiles;
        xmlPopulatedReport.avgNumOfXMLSimpleElements = (double) xmlPopulatedReport.totNumOfXMLSimpleElements / fileReport.numOfFiles;
        xmlPopulatedReport.avgXMLEmptyElement = (double) xmlPopulatedReport.totNumOfXMLEmptyElement / fileReport.numOfFiles;

        xmlPopulatedReport.avgRateOfPopulatedElements = xmlPopulatedReport.avgNumOfXMLSimpleElements == 0 ? 0 : (xmlPopulatedReport.avgNumOfXMLSimpleElements - xmlPopulatedReport.avgXMLEmptyElement) / xmlPopulatedReport.avgNumOfXMLSimpleElements;


        //statistics

        if (Configuration.DATABASE) {
            MongoClient mongoClient;
            if (Configuration.DATABASE_URI == null || Configuration.DATABASE_URI.isEmpty()) {//if it is empty, try localhost
                mongoClient = MongoClients.create();
            } else {
                mongoClient = MongoClients.create(Configuration.DATABASE_URI);
            }

            MongoDatabase database = mongoClient.getDatabase(Configuration.DATABASE_NAME);

            MongoCollection<Document> linksChecked = database.getCollection("linksChecked");
            MongoCollection<Document> linksToBeChecked = database.getCollection("linksToBeChecked");

            AggregateIterable<Document> iterable = linksChecked.aggregate(Arrays.asList(
                    Aggregates.match(eq("collection", getName())),
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
                statistics.count = doc.getInteger("count");
                urlReport.status.add(statistics);
            }


            long numOfCheckedLinks = linksChecked.countDocuments(eq("collection", getName()));

            Bson brokenLinksFilter = Filters.and(Filters.eq("collection", getName()), Filters.and(Filters.not(Filters.eq("status", 200)), Filters.not(Filters.eq("status", 302))));
            long numOfBrokenLinks = linksChecked.countDocuments(brokenLinksFilter);

            urlReport.totNumOfBrokenLinks = (int) numOfBrokenLinks;
            urlReport.totNumOfCheckedLinks = (int) (numOfCheckedLinks);

            //joined urls from linkstobechecked
            //to calculate unique links, linkchecked and linkstobechecked are added together and duplicates are subtracted.
            //if this operation is not done through the database, the value would be wrong.
            //because url validator works on a record basis and not collection basis, so the program
            //can only know about unique link numbers in a single record and not the whole collection.
            //thats why there is some database magic on the whole collection needed.
            //there might be mongo only way to do this but i dont know.
            iterable = linksToBeChecked.aggregate(Arrays.asList(
                    Aggregates.match(eq("collection", getName())),
                    Aggregates.lookup("linksChecked", "url", "url", "checked")
            ));
            int duplicates = 0;
            for (Document doc : iterable) {
                if (!((List) doc.get("checked")).isEmpty()) {
                    duplicates++;
                }
            }

            int numOfLinksToBeChecked = (int) linksToBeChecked.countDocuments(eq("collection", getName()));
            urlReport.totNumOfUniqueLinks = ((int) numOfCheckedLinks) + numOfLinksToBeChecked - duplicates;


            urlReport.avgNumOfLinks = (double) urlReport.totNumOfLinks / fileReport.numOfFiles;
            urlReport.avgNumOfUniqueLinks = (double) urlReport.totNumOfUniqueLinks / fileReport.numOfFiles;
            urlReport.avgNumOfBrokenLinks = 1.0 * (double) urlReport.totNumOfBrokenLinks / fileReport.numOfFiles;

            //todo avgnumofresproxies is not calculated anywhere, do it by calculating from mime type from database
            urlReport.avgNumOfResProxiesLinks = (double) urlReport.totNumOfResProxiesLinks / fileReport.numOfFiles;


        }


//        urlReport.ratioOfValidLinks = urlReport.totNumOfUniqueLinks == 0 ? 0 :
//                (double) (urlReport.totNumOfUniqueLinks - urlReport.totNumOfBrokenLinks) / urlReport.totNumOfUniqueLinks;

        urlReport.ratioOfValidLinks = urlReport.totNumOfCheckedLinks == 0 ? 0 :
                (double) (urlReport.totNumOfCheckedLinks - urlReport.totNumOfBrokenLinks) / urlReport.totNumOfCheckedLinks;

        // Facets
        facetReport.facet.forEach(facet -> facet.coverage = (double) facet.cnt / fileReport.numOfFiles);
        facetReport.coverage = facetReport.facet.stream().mapToDouble(f -> f.cnt != 0 ? 1.0 : 0).sum() / facetReport.facet.size();

        avgScore = fileReport.numOfFiles == 0 ? 0.0 : score / fileReport.numOfFiles;
        maxScore = fileReport.numOfFiles * maxPossibleScoreInstance;

        scorePercentage = maxScore == 0.0 ? 0.0 : score / maxScore;

    }

    @Override
    public void toXML(OutputStream os) throws Exception {
        XMLMarshaller<CollectionReport> instanceMarshaller = new XMLMarshaller<>(CollectionReport.class);
        instanceMarshaller.marshal(this, os);
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FileReport {
        public String provider;
        public long numOfFiles = 0;
        public long size;
        public long avgSize;
        public long minFileSize;
        public long maxFileSize;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class HeaderReport {
        @XmlElementWrapper(name = "duplicatedMDSelfLinks")
        public Collection<String> duplicatedMDSelfLink = null;
        public Profiles profiles = null;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ResProxyReport {
        public int totNumOfResProxies;
        public Double avgNumOfResProxies = 0.0;
        public int totNumOfResourcesWithMime;
        public Double avgNumOfResourcesWithMime = 0.0;
        public int totNumOfResProxiesWithReferences;
        public Double avgNumOfResProxiesWithReferences = 0.0;

    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XMLPopulatedReport {
        public int totNumOfXMLElements;
        public Double avgNumOfXMLElements = 0.0;
        public int totNumOfXMLSimpleElements;
        public Double avgNumOfXMLSimpleElements = 0.0;
        public int totNumOfXMLEmptyElement;
        public Double avgXMLEmptyElement = 0.0;
        public Double avgRateOfPopulatedElements = 0.0;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XMLValidationReport {
        public int totNumOfRecords;
        public int totNumOfValidRecords;
        public Double ratioOfValidRecords = 0.0;
        @XmlElementWrapper(name = "invalid-records")
        public Collection<String> record = new ArrayList<>();
    }

    @XmlRootElement
    public static class URLValidationReport {
        public int totNumOfLinks;
        public Double avgNumOfLinks = 0.0;
        public int totNumOfUniqueLinks;
        public int totNumOfCheckedLinks;
        public Double avgNumOfUniqueLinks = 0.0;
        public int totNumOfResProxiesLinks;
        public Double avgNumOfResProxiesLinks = 0.0;
        public int totNumOfBrokenLinks;
        public Double avgNumOfBrokenLinks = 0.0;
        public Double ratioOfValidLinks = 0.0;
        @XmlElementWrapper(name = "statistics")
        public Collection<Statistics> status = new ArrayList<>();
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Statistics {
        @XmlAttribute
        public int count;

        @XmlAttribute
        public double avgRespTime;

        @XmlAttribute
        public double maxRespTime;

        @XmlAttribute
        public int statusCode;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FacetReport {
        public Double coverage = 0.0;

        @XmlElementWrapper(name = "facets")
        public Collection<FacetCollectionStruct> facet;
    }

    @XmlRootElement
    public static class FacetCollectionStruct {
        @XmlAttribute
        public String name;

        @XmlAttribute
        public int cnt; //num of records covering it

        @XmlAttribute
        public Double coverage;

    }

    @XmlRootElement(name = "profiles")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Profiles {

        @XmlAttribute(name = "count")
        public int totNumOfProfiles = 0;

        public List<Profile> profiles;

        public void handleProfile(String profile, double score) {
            if (profiles == null)
                profiles = new ArrayList<>();

            for (Profile p : profiles)
                if (p.name.equals(profile)) {
                    p.count++;
                    return;
                }

            Profile p = new Profile();
            p.count = 1;
            p.name = profile;
            p.score = score;

            profiles.add(p);
            totNumOfProfiles++;
        }

        public void handleProfile(Profile profile) {
            if (profiles == null)
                profiles = new ArrayList<>();

            for (Profile p : profiles)
                if (p.name.equals(profile.name)) {
                    p.count += profile.count;
                    return;
                }

            profiles.add(profile);
            totNumOfProfiles++;
        }

    }

    @XmlRootElement(name = "profile")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Profile {

        @XmlAttribute
        public String name;

        @XmlAttribute
        public int count;

        @XmlAttribute
        public Double score;
    }

}

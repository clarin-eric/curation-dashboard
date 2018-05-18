package eu.clarin.cmdi.curation.report;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.*;

import eu.clarin.cmdi.curation.xml.XMLMarshaller;

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

    @XmlAttribute(name = "ins-max-score")
    public Double maxScoreInstance = 0.0;

    @XmlAttribute
    public Long timeStamp = System.currentTimeMillis();

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
    @XmlElementWrapper(name = "invalid-records")
    public Collection<InvalidFile> record;

    @XmlRootElement
    public static class InvalidFile {
        @XmlValue
        public String recordName;

        @XmlAttribute(name = "reason")
        public String reason;
    }

    public void addInvalidFile(InvalidFile invalidFile) {
        if (this.record == null) {
            this.record = new ArrayList<>();
        }
        this.record.add(invalidFile);
    }

    // URLs
    @XmlElementWrapper(name = "urls")
    public Collection<CMDInstanceReport.URLElement> url;

    @XmlRootElement
    public static class URLElement {
        @XmlValue
        public String url;

        @XmlAttribute(name = "http-status")
        public String status;

        @XmlAttribute(name = "content-type")
        public String contentType;

        @XmlAttribute(name = "byte-size")
        public String byteSize;

        @XmlAttribute(name = "request-duration")
        public String duration;

        @XmlAttribute(name = "timestamp")
        public String timestamp;
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

        // XMLPopulatedValidator
        parentReport.xmlPopulatedReport.totNumOfXMLElements += xmlPopulatedReport.totNumOfXMLElements;
        parentReport.xmlPopulatedReport.totNumOfXMLSimpleElements += xmlPopulatedReport.totNumOfXMLSimpleElements;
        parentReport.xmlPopulatedReport.totNumOfXMLEmptyElement += xmlPopulatedReport.totNumOfXMLEmptyElement;

        // URL
        parentReport.urlReport.totNumOfLinks += urlReport.totNumOfLinks;
        parentReport.urlReport.totNumOfUniqueLinks += urlReport.totNumOfUniqueLinks;
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
        if (this.record != null) {
            if (parentReport.record == null)
                parentReport.record = new ArrayList<>();
            parentReport.record.addAll(this.record);
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
        xmlValidationReport.avgRateOfValidRecords = (double) xmlValidationReport.totNumOfValidRecords / xmlValidationReport.totNumOfRecords;

        // XMLPopulatedValidator
        xmlPopulatedReport.avgNumOfXMLElements = (double) xmlPopulatedReport.totNumOfXMLElements / fileReport.numOfFiles;
        xmlPopulatedReport.avgNumOfXMLSimpleElements = (double) xmlPopulatedReport.totNumOfXMLSimpleElements / fileReport.numOfFiles;
        xmlPopulatedReport.avgXMLEmptyElement = (double) xmlPopulatedReport.totNumOfXMLEmptyElement / fileReport.numOfFiles;

        xmlPopulatedReport.avgRateOfPopulatedElements = (xmlPopulatedReport.avgNumOfXMLSimpleElements - xmlPopulatedReport.avgXMLEmptyElement) / xmlPopulatedReport.avgNumOfXMLSimpleElements;

        // URL
        urlReport.avgNumOfLinks = (double) urlReport.totNumOfLinks / fileReport.numOfFiles;
        urlReport.avgNumOfUniqueLinks = (double) urlReport.totNumOfUniqueLinks / fileReport.numOfFiles;
        urlReport.avgNumOfResProxiesLinks = (double) urlReport.totNumOfResProxiesLinks / fileReport.numOfFiles;
        urlReport.avgNumOfBrokenLinks = 1.0 * (double) urlReport.totNumOfBrokenLinks / fileReport.numOfFiles;

        urlReport.avgNumOfValidLinks = (double) (urlReport.totNumOfUniqueLinks - urlReport.totNumOfBrokenLinks)
                / fileReport.numOfFiles;

        // Facets
        facetReport.facet.forEach(facet -> facet.coverage = (double) facet.cnt / fileReport.numOfFiles);
        facetReport.coverage = facetReport.facet.stream().mapToDouble(f -> f.cnt != 0 ? 1.0 : 0).sum() / facetReport.facet.size();

        avgScore = score / fileReport.numOfFiles;
        maxScore = fileReport.numOfFiles * maxScoreInstance;

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
        public Double avgRateOfValidRecords = 0.0;
    }

    @XmlRootElement
    public static class URLValidationReport {
        public int totNumOfLinks;
        public Double avgNumOfLinks = 0.0;
        public int totNumOfUniqueLinks;
        public Double avgNumOfUniqueLinks = 0.0;
        public int totNumOfResProxiesLinks;
        public Double avgNumOfResProxiesLinks = 0.0;
        public int totNumOfBrokenLinks;
        public Double avgNumOfBrokenLinks = 0.0;
        public Double avgNumOfValidLinks = 0.0;
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

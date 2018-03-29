package eu.clarin.cmdi.curation.report;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.report.CollectionReport.FacetCollectionStruct;
import eu.clarin.cmdi.curation.xml.XMLMarshaller;

/**
 * @author dostojic
 */

@XmlRootElement(name = "instance-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class CMDInstanceReport implements Report<CollectionReport> {

    @XmlAttribute
    public Double score = 0.0;

    @XmlAttribute(name = "ins-score")
    public Double instanceScore = 0.0;

    @XmlAttribute(name = "pfl-score")
    public Double profileScore = 0.0;

    @XmlAttribute(name = "max-score")
    public double maxScore;

    @XmlAttribute
    public Long timeStamp = System.currentTimeMillis();

    // sub reports **************************************

    // Header
    @XmlElement(name = "profile-section")
    public ProfileHeader header;

    // file
    @XmlElement(name = "file-section")
    public FileReport fileReport;

    // ResProxy
    @XmlElement(name = "resProxy-section")
    public ResProxyReport resProxyReport;

    // XMLValidator
    @XmlElement(name = "xml-validation-section")
    public XMLReport xmlReport;

    // URL
    @XmlElement(name = "url-validation-section")
    public URLReport urlReport;

    // facets
    @XmlElement(name = "facets-section")
    public FacetReport facets;

    //scores
    @XmlElementWrapper(name = "score-section")
    @XmlElement(name = "score")
    public Collection<Score> segmentScores;

    @Override
    public String getName() {
        if (fileReport.location.contains(".xml")) {
            String normalisedPath = fileReport.location.replace('\\', '/');
            return normalisedPath.substring(normalisedPath.lastIndexOf('/') + 1, normalisedPath.lastIndexOf('.'));
        } else {
			return fileReport.location;
        }

    }

    @Override
    public void mergeWithParent(CollectionReport parentReport) {

        parentReport.score += score;
        if (score > parentReport.insMaxScore)
            parentReport.insMaxScore = score;

        if (score < parentReport.insMinScore)
            parentReport.insMinScore = score;

        parentReport.maxScoreInstance = maxScore;

        // ResProxies
        parentReport.resProxyReport.totNumOfResProxies += resProxyReport.numOfResProxies;

        parentReport.resProxyReport.totNumOfResourcesWithMime += resProxyReport.numOfResourcesWithMime;
        parentReport.resProxyReport.totNumOfResProxiesWithReferences += resProxyReport.numOfResProxiesWithReferences;

        // XMLValidator
        parentReport.xmlReport.totNumOfXMLElements += xmlReport.numOfXMLElements;
        parentReport.xmlReport.totNumOfXMLSimpleElements += xmlReport.numOfXMLSimpleElements;
        parentReport.xmlReport.totNumOfXMLEmptyElement += xmlReport.numOfXMLEmptyElement;

        // URL
        parentReport.urlReport.totNumOfLinks += urlReport.numOfLinks;
        parentReport.urlReport.totNumOfUniqueLinks += urlReport.numOfUniqueLinks;
        parentReport.urlReport.totNumOfBrokenLinks += urlReport.numOfBrokenLinks;

        // Facet
        facets.coverage.stream()
                .filter(facet -> facet.coveredByInstance)
                .map(facet -> facet.name)
                .forEach(coveredFacet -> {
                    FacetCollectionStruct parFacet = parentReport.facetReport.facet.stream().filter(f -> f.name.equals(coveredFacet)).findFirst().orElse(null);
                    if (parFacet != null) {//in case of derived facet parFacet will be null
                        parFacet.cnt++;
                    }
                });

        parentReport.handleProfile(header.id, profileScore);

    }

    @Override
    public void toXML(OutputStream os) throws Exception {
        XMLMarshaller<CMDInstanceReport> instanceMarshaller = new XMLMarshaller<>(CMDInstanceReport.class);
        instanceMarshaller.marshal(this, os);
    }

    @Override
    public boolean isValid() {
        return segmentScores.stream().filter(Score::hasFatalMsg).findFirst().orElse(null) == null;
    }

    @Override
    public void addSegmentScore(Score segmentScore) {
        if (segmentScores == null)
            segmentScores = new ArrayList<>();

        segmentScores.add(segmentScore);
        maxScore += segmentScore.maxScore;
        score += segmentScore.score;
        if (!segmentScore.segment.equals("profiles-score"))
            instanceScore += segmentScore.score;

    }


    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FileReport {
        public String location;
        public long size;
        public String collection;

        public FileReport() {
        }

        ;
    }


    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ResProxyReport {
        public int numOfResProxies;
        public int numOfResourcesWithMime;
        public Double percOfResourcesWithMime;
        public int numOfResProxiesWithReferences;
        public Double percOfResProxiesWithReferences;

        @XmlElementWrapper(name = "resourceTypes")
        public Collection<ResourceType> resourceType;

        public ResProxyReport() {
        }

    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ResourceType {
        @XmlAttribute
        public String type;

        @XmlAttribute
        public int count;

    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class XMLReport {
        public int numOfXMLElements;
        public int numOfXMLSimpleElements;
        public int numOfXMLEmptyElement;
        public Double percOfPopulatedElements;

        public XMLReport() {
        }
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class URLReport {
        public int numOfLinks;
        public int numOfUniqueLinks;
        public int numOfBrokenLinks;
        public Double percOfValidLinks;

        public URLReport() {
        }

    }

}

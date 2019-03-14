package eu.clarin.cmdi.curation.statistics;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.report.CollectionReport;

/**
 * A selection of values from a single CollectionReport which will form a line in a statistical overview
 * @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 *
 */
@XmlRootElement(name = "collection")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionStatistics {
    @XmlAttribute
    private String name;
    @XmlElement
    private double scorePercentage;
    @XmlElement    
    private long numOfFiles;
    @XmlElement
    private int numOfProfiles;
    @XmlElement
    private int numOfUniqueLinks;
    @XmlElement
    private int numOfCheckedLinks;
    @XmlElement
    private double ratioOfValidLinks;
    @XmlElement
    private double avgNumOfResProxies;    
    @XmlElement
    private int numOfResProxies;
    @XmlElement
    private double ratioOfValidRecords;
    @XmlElement
    private double avgNumOfEmptyXMLElements;
    @XmlElement
    private double avgFacetCoverage;    
    @XmlElement(name="facet")
    private List<Facet> facets = new ArrayList<Facet>();
    
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)    
    public static class Facet{
        @XmlAttribute
        private String name;
        @XmlElement
        private double avgCoverage;
        
        public Facet(String name, double avgCoverage) {
            this.name = name;
            this.avgCoverage = avgCoverage;
        }
    }
    
    public CollectionStatistics(CollectionReport report) {
        this.name = report.getName();
        this.scorePercentage = report.avgScore;
        this.numOfFiles = report.fileReport.numOfFiles;
        this.numOfProfiles = report.headerReport.profiles.totNumOfProfiles;
        this.numOfUniqueLinks = report.urlReport.totNumOfUniqueLinks;
        this.numOfCheckedLinks = report.urlReport.totNumOfCheckedLinks;
        this.ratioOfValidLinks = report.urlReport.ratioOfValidLinks;
        this.avgNumOfResProxies = report.resProxyReport.avgNumOfResProxies;
        this.numOfResProxies = report.resProxyReport.totNumOfResProxies;
        this.ratioOfValidRecords = report.xmlValidationReport.ratioOfValidRecords;
        this.avgNumOfEmptyXMLElements = report.xmlPopulatedReport.avgXMLEmptyElement;
        
        report.facetReport.facet.forEach(f -> facets.add(new Facet(f.name, f.coverage)));
    }
}

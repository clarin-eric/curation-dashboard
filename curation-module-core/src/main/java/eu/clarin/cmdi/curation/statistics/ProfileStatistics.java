package eu.clarin.cmdi.curation.statistics;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.report.CMDProfileReport;

@XmlRootElement(name = "profile")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfileStatistics {
    @XmlAttribute
    private String id;
    @XmlElement
    private String name;
    @XmlElement
    private double score;
    @XmlElement
    private double facetCoverage;
    @XmlElement
    private double percOfElementsWithConcept;
    @XmlElement
    private List<Facet> facets = new ArrayList<Facet>();
    
    public ProfileStatistics(CMDProfileReport report) {
        this.id = report.header.getId();
        this.name = report.header.getName();
        this.score = report.score;        
        this.facetCoverage = report.facet.profileCoverage;
        
        report.facet.coverage.forEach(f -> facets.add(new Facet(f.name, f.coveredByProfile)));
    }
    
    @XmlRootElement
    private static class Facet{
        @XmlAttribute
        private String name;
        @XmlElement
        private boolean covered;
        public Facet(String name, boolean covered) {
            this.name = name;
            this.covered = covered;
        }
    }
	
}

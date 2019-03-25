package eu.clarin.cmdi.curation.report;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
@XmlRootElement(name = "collections")
@XmlAccessorType(XmlAccessType.FIELD)
public class ProfilesReport implements Report<ProfilesReport> {

    
    @XmlElement(name="profile")
    private List<Profile> profiles = new ArrayList<Profile>();

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
        return "ProfilesReport";
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
    public void mergeWithParent(ProfilesReport parentReport) {
        // TODO Auto-generated method stub
        
    }
    
    public void addReport(Report report) {
        if(report instanceof CMDProfileReport) {
            
            this.profiles.add(new Profile((CMDProfileReport) report));
            
        }
    }
    

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)    
    public static class Profile{

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
        
        @XmlElementWrapper(name="facets")
        @XmlElement(name="facet")
        private List<Facet> facets = new ArrayList<Facet>();  
        
        public Profile() {
            
        }
        
        public Profile(CMDProfileReport report) {
            this.id = report.header.getId();
            this.name = report.header.getName();
            this.score = report.score;        
            this.facetCoverage = report.facet.profileCoverage;
            
            report.facet.coverage.forEach(f -> facets.add(new Facet(f.name, f.coveredByProfile)));          
        }
        
    }
    
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)    

    private static class Facet{
        @XmlAttribute
        private String name;
        @XmlElement
        private boolean covered;
        
        public Facet() {
            
        }
        public Facet(String name, boolean covered) {
            this.name = name;
            this.covered = covered;
        }
    }
}

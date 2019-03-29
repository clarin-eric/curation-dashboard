package eu.clarin.cmdi.curation.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.report.CollectionReport.Statistics;

/*
* @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
*/
@XmlRootElement(name = "linkchecker-report")
@XmlAccessorType(XmlAccessType.FIELD)
public class LinkCheckerReport implements Report<LinkCheckerReport> {
    
    @XmlElement(name="collection")
    private List<Collection> collections;

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
        return null;
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
    
    public void addCollectionReport(Report report) {
        if(report instanceof CollectionReport) {
            CollectionReport cReport = (CollectionReport) report;
            

        }
    }
    
    private static class Collection{
        @XmlAttribute
        private String name;
        
        @XmlElement
        private java.util.Collection<Statistics> statistics;
        
        public Collection() {
            
        }
    }
    


}

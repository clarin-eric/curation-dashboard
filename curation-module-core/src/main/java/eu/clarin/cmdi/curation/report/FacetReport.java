package eu.clarin.cmdi.curation.report;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author dostojic
 *
 */
@XmlRootElement(name = "facets")
@XmlAccessorType(XmlAccessType.FIELD)
public class FacetReport {

	@XmlAttribute
    public int numOfFacets;
	
	@XmlElementWrapper
	@XmlElement(name="facet")
	public Collection<Coverage> coverage;
	
	@XmlAttribute
	public Double profileCoverage;
	
	@XmlAttribute
	public Double instanceCoverage;	
	
	@XmlElementWrapper
	@XmlElement(name="valueNode")
	public Collection<ValueNode> values;

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ValueNode{
    	
    	@XmlElement public String value;
    	
    	@XmlElement public String xpath;
    	
    	@XmlElement	public Concept concept;
    	
    	@XmlElement public Collection<FacetValueStruct> facet;
    		
    }    
    
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FacetValueStruct{
    	
    	@XmlAttribute public String name;
		
    	@XmlAttribute public Boolean isDerived;
    	
    	@XmlAttribute public Boolean usesValueMapping;
    	
    	@XmlAttribute public String normalisedValue;    
		
    }
    
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Coverage{
    	
    	@XmlAttribute public String name;
		
    	@XmlAttribute public Boolean coveredByProfile = false;
    	
    	@XmlAttribute public Boolean coveredByInstance = false;
    	
    	@Override
    	public String toString() {
    		return name + "\tprofile: " + coveredByProfile + "\tinstance: " + coveredByInstance;
    	}
		
    }
    
}
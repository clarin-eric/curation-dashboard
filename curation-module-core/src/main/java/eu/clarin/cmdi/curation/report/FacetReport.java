/**
 * 
 */
package eu.clarin.cmdi.curation.report;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * @author dostojic
 *
 */
@XmlRootElement(name = "facets")
@XmlAccessorType(XmlAccessType.FIELD)
public class FacetReport {

    public int numOfFacets;

    public Profile profile;

    public Instance instance;
    
    @XmlElementWrapper(name = "details")
    public List<Message> messages = null;

    @XmlRootElement(name = "profile")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Profile {
	public int numOfCoveredFacets;
	public Double coverage;
	@XmlElementWrapper(name = "not-covered")
	@XmlElement(name = "facet")
	public List<String> notCovered;

    }

    @XmlRootElement(name = "instance")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Instance {
	public int numOfCoveredFacets;
	public Double coverage;

	public List<FacetValues> facet;

	@XmlElementWrapper(name = "missingValues")
	@XmlElement(name = "facet")
	public List<String> missingValue;

    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FacetValues {

	public FacetValues() {
	}

	public FacetValues(String name, List<FacetValue> values) {
	    this.name = name;
	    this.value = values;
	}

	@XmlAttribute
	public String name;
	
	@XmlElement(name = "entry")
	public List<FacetValue> value;
    
    
    }
    
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FacetValue{
    	
    	@XmlAttribute
    	public String value;    	
    	
    	@XmlAttribute
    	public String concept;
    	
    	@XmlAttribute
    	public String xpath;
    	
    	
    	public FacetValue() {
		}

		public FacetValue(String concept, String xpath, String value) {
			this.concept = concept;
			this.xpath = xpath;
			this.value = value;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof FacetValue))
	            return false;
	        if (obj == this)
	            return true;
	        FacetValue rhs = (FacetValue) obj;
	        return new EqualsBuilder()
	        		.append(value, rhs.value)
	        		.append(xpath, rhs.xpath)
	        		.isEquals();
		}
		
		@Override
		public String toString() {
			return value + "\t" + xpath + "\t" + concept;
		}
    	
    	
    }

}
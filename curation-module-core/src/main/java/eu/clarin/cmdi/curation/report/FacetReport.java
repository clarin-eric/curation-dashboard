/**
 * 
 */
package eu.clarin.cmdi.curation.report;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.EqualsBuilder;

/**
 * @author dostojic
 *
 */
@XmlRootElement(name = "facets")
@XmlAccessorType(XmlAccessType.FIELD)
public class FacetReport {

	@XmlAttribute
    public int numOfFacets;
	
	@XmlAttribute
	public Integer coveredByProfile;
	
	@XmlAttribute
	public Integer coveredByInstance;
	
	@XmlAttribute
	public Double profileCoverage;
	
	//keep it as Object to remove from profile report
	@XmlAttribute
	public Double instanceCoverage;
	
	
	@XmlElement(name = "facet")
	public Collection<FacetStruct> facets;
	
	@XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FacetStruct{
    	@XmlAttribute
    	public String name;
    	
    	@XmlAttribute
    	public boolean covered;
    	
    	@XmlAttribute
    	public Boolean derived = null;
    	
    	@XmlElement(name = "entry")
		public Collection<FacetValue> values;
    	
    }

    
    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FacetValue{
    	
    	@XmlAttribute
    	public String value;
    	
    	@XmlAttribute
    	public String normalisedValue;
    	
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
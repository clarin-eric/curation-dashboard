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

	@XmlElementWrapper(name = "values")
	public List<FacetValues> facet;

	@XmlElementWrapper(name = "missingValues")
	public List<FacetValues> missingValues;

    }

    @XmlRootElement(name = "facet")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class FacetValues {

	public FacetValues() {
	}

	public FacetValues(String name, List<String> values) {
	    this.name = name;
	    this.value = values;
	}

	@XmlAttribute
	public String name;

	@XmlElementWrapper(name = "values")
	public List<String> value;
    }

}
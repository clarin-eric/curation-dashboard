package eu.clarin.cmdi.curation.entities;

import java.nio.file.Path;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.processor.AbstractProcessor;
import eu.clarin.cmdi.curation.processor.CMDIProfileProcessor;

/**
 * @author dostojic
 *
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "profileDescription")
public class CMDIProfile extends CurationEntity {

    @XmlElement
    String id;
    @XmlElement
    String name;
    @XmlElement
    String url;

    @XmlAttribute
    boolean isPublic;

    @XmlElement
    double facetCoverage;

    @XmlElementWrapper(name = "coveredFacets")
    @XmlElement
    Collection<String> facet;

    public CMDIProfile(Path path) {
	super(path);
    }

    public CMDIProfile(Path path, long size) {
	super(path, size);
    }

    @Override
    public String toString() {
	return new StringBuffer(1000).append("Id: ").append(id).append("\t").append("Name: ").append(name).append("\t")
		.append("URL: ").append(url).toString();
    }

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getUrl() {
	return url;
    }

    public void setUrl(String url) {
	this.url = url;
    }

    public boolean isPublic() {
	return isPublic;
    }

    public void setPublic(boolean isPublic) {
	this.isPublic = isPublic;
    }

    public double getFacetCoverage() {
	return facetCoverage;
    }

    public void setFacetCoverage(double facetCoverage) {
	this.facetCoverage = facetCoverage;
    }

    public Collection<String> getCoveredFacets() {
	return facet;
    }

    public void setCoveredFacets(Collection<String> facet) {
	this.facet = facet;
    }

    @Override
    protected AbstractProcessor getProcessor() {
	return new CMDIProfileProcessor();
    }

}

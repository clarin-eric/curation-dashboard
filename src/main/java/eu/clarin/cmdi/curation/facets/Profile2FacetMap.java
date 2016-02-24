/**
 * 
 */
package eu.clarin.cmdi.curation.facets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dostojic
 *
 */
public class Profile2FacetMap {
    

    private Collection<Facet> mappings = new LinkedList<>();

    private List<String> notCovered = new ArrayList<String>();

    public void addMapping(Facet facet) {
	mappings.add(facet);
    }

    public Collection<Facet> getMappings() {
	return mappings;
    }

    public Collection<String> getFacetNames() {
	return mappings.stream().map(Facet::getName).collect(Collectors.toList());
    }

    public List<String> getNotCovered() {
	return notCovered;
    }

    public void addNotCovered(String facet) {
	this.notCovered.add(facet);
    }

    @Override
    public String toString() {
	StringBuilder sb = new StringBuilder();
	mappings.stream().map(f -> sb.append(f.toString()).append("\n"));
	return sb.toString();
    }

    public static class Facet {
	private String name;
	private boolean caseInsensitive = false;
	private List<String> patterns = new ArrayList<String>();
	private List<String> fallbackPatterns = new ArrayList<String>();
	private List<String> derivedFacets = new ArrayList<String>();
	private boolean allowMultipleValues = true;

	public void setCaseInsensitive(boolean caseValue) {
	    this.caseInsensitive = caseValue;
	}

	public boolean isCaseInsensitive() {
	    return caseInsensitive;
	}

	public void setPatterns(List<String> patterns) {
	    this.patterns = patterns;
	}

	public void setFallbackPatterns(List<String> fallbackPatterns) {
	    this.fallbackPatterns = fallbackPatterns;
	}

	public void setPattern(String pattern) {
	    this.patterns = Collections.singletonList(pattern);
	}

	public void setFallbackPattern(String fallbackPattern) {
	    this.fallbackPatterns = Collections.singletonList(fallbackPattern);
	}

	/**
	 * @return List of Strings which are xpaths expressions.
	 */
	public List<String> getPatterns() {
	    return patterns;
	}

	public List<String> getFallbackPatterns() {
	    return fallbackPatterns;
	}

	public void setName(String name) {
	    this.name = name;
	}

	public String getName() {
	    return name;
	}

	@Override
	public String toString() {
	    return "name=" + name + ", pattern=" + patterns + ", fallbackpattern=" + fallbackPatterns;
	}

	public boolean getAllowMultipleValues() {
	    return allowMultipleValues;
	}

	public void setAllowMultipleValues(boolean allowMultipleValues) {
	    this.allowMultipleValues = allowMultipleValues;
	}

	public List<String> getDerivedFacets() {
	    return derivedFacets;
	}

	public void setDerivedFacets(List<String> derivedFacets) {
	    this.derivedFacets = derivedFacets;
	}

    }

}

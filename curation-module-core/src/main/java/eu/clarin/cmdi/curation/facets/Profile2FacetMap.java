package eu.clarin.cmdi.curation.facets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author dostojic
 *
 */
public class Profile2FacetMap {

	private Map<String, Facet> mappings = new HashMap<>();
	
	void addMapping(String name, Facet facet) {
		mappings.put(name, facet);
	}

	public Map<String, Facet> getMappings() {
		return mappings;
	}

	public Collection<String> getFacetNames() {
		return mappings.keySet();
	}

	public Collection<String> getCovered() {
		return mappings.keySet().stream().filter(name -> mappings.get(name) != null).collect(Collectors.toList());
	}
	
	public Collection<String> getNotCovered() {
		return mappings.keySet().stream().filter(name -> mappings.get(name) == null).collect(Collectors.toList());
	}


	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		mappings.keySet().forEach(name -> sb.append(name + ":\n" + mappings.get(name)).append("\n"));
		return sb.toString();
	}

	public static class Facet {
		private boolean caseInsensitive = false;
		private Map<String, String> patterns = new HashMap<>(); //extended to keep concepts
		private Collection<String> fallbackPatterns = new ArrayList<String>();
		private Collection<String> derivedFacets = new ArrayList<String>();
		private boolean allowMultipleValues = true;

		public void setCaseInsensitive(boolean caseValue) {
			this.caseInsensitive = caseValue;
		}

		public boolean isCaseInsensitive() {
			return caseInsensitive;
		}

		public void setPatterns(Map<String, String> patterns) {
			this.patterns = patterns;
		}

		public void setFallbackPatterns(Collection<String> fallbackPatterns) {
			this.fallbackPatterns = fallbackPatterns;
		}

		public void setFallbackPattern(String fallbackPattern) {
			this.fallbackPatterns = Collections.singletonList(fallbackPattern);
		}

		/**
		 * @return List of Strings which are xpaths expressions.
		 */
		public Map<String, String> getPatterns() {
			return patterns;
		}

		public Collection<String> getFallbackPatterns() {
			return fallbackPatterns;
		}


		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("name:").append("\n").append("patterns:").append("\n");
			patterns.forEach((k, v) -> sb.append("\t" + k + "\t" + v).append("\n"));
			sb.append("fallback patterns:").append("\n");
			fallbackPatterns.forEach(p -> sb.append("\t" + p).append("\n"));
			
			return sb.toString();
		}

		public boolean getAllowMultipleValues() {
			return allowMultipleValues;
		}

		public void setAllowMultipleValues(boolean allowMultipleValues) {
			this.allowMultipleValues = allowMultipleValues;
		}

		public Collection<String> getDerivedFacets() {
			return derivedFacets;
		}

		public void setDerivedFacets(Collection<String> collection) {
			this.derivedFacets = collection;
		}

	}

}

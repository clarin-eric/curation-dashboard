package eu.clarin.cmdi.curation.facets.controlled_vocabulary;

import java.util.ArrayList;
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


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "mappings")
class VariantsMap{
	
	@XmlAttribute
	String field;
	
	@XmlElement(name = "mapping")
	List<Mapping> mappings;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(field + "\n");
		mappings.forEach(m -> sb.append(m.toString()));
		return sb.toString();
	}
	
	
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Mapping{
				
		@XmlElement(name = "normalizedValue")
		NormalizedValue normalizedVlalue;
		
		@XmlElement(name = "variant")
		List<Variant> variants = new ArrayList<Variant>();

		
		@Override
		public String toString() {			
			StringBuilder sb = new StringBuilder(normalizedVlalue + " variants[");
			variants.forEach(v -> sb.append("\n\t" + v));
			return sb.append("]\n").toString();
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static class NormalizedValue {
		
		@XmlAttribute
		String value;
		
		@Override
		public String toString() {
			return value;
		}
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	static class Variant{
		
		@XmlAttribute
		String value;
		
		@XmlAttribute
		Boolean isRegExp = false;
		
		@XmlElementWrapper
		@XmlElement(name = "cross-mapping")
		List<CrossMapping> crossMappings;
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(value + " " + (isRegExp ? "isRegEx = true" : "") + " " + "cross-mappings[");
			crossMappings.forEach(cm -> sb.append("\n\t\t" + cm));
			
			return sb.toString() + "]\n";
		}		
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "cross-mapping")
	static class CrossMapping {
		
		@XmlAttribute
		String facet;
		
		@XmlAttribute
		String value;
		
		@Override
		public String toString() {
			return facet + ":" + value;
		}
	}
	
}

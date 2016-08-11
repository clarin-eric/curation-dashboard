package eu.clarin.cmdi.curation.facets;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import eu.clarin.cmdi.curation.xml.XMLMarshaller;

/**
 * Corresponds to the facet concepts file.
 *
 * This class holds the mapping of facet name -> facetConcepts/patterns A
 * facetConcept is a ISOcat conceptLink e.g.:
 * http://www.isocat.org/datcat/DC-2544 the conceptLink will be analysed and
 * translated into a valid Xpath expression to extract data out of the metadata.
 * Valid xpath expression e.g. /c:CMD/c:Header/c:MdSelfLink/text(), the 'c'
 * namespace will be mapped to http://www.clarin.eu/cmd/ in the parser. A
 * pattern is an xpath expression used directly on the metadata. Use patterns
 * only when a conceptLink does not suffice.
 *
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class FacetConcepts {

	@XmlElement(name = "facetConcept")
	Collection<FacetConcept> facetConcepts;
	
	
	public static Collection<FacetConcept> createFacetConcept(){
		try{
			XMLMarshaller<FacetConcepts> facetConceptMarshaller = new XMLMarshaller<>(FacetConcepts.class);
			Collection<FacetConcept> facetConcepts = facetConceptMarshaller
					.unmarshal(new URL(FacetConceptMappingService.FACET_CONCEPTS_URL).openStream())
					.facetConcepts;
			//normalise -> stripe cmd(p) namespace
			facetConcepts.forEach(fc->{				
				fc.patterns = fc.patterns.stream().map(p -> p.replaceAll("cmd:", "").replaceAll("cmdp:", "")).collect(Collectors.toList());
				fc.blacklistPatterns = fc.blacklistPatterns.stream().map(p -> p.replaceAll("cmd:", "").replaceAll("cmdp:", "")).collect(Collectors.toList());
				
			});
			
			
			return facetConcepts;
		}catch(Exception e){
			throw new RuntimeException("Unable to read file" + FacetConceptMappingService.FACET_CONCEPTS_URL + "!", e);
		}
		
	}
	
	
	
	@XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "facetConcept")
    public static class FacetConcept {

        @XmlAttribute
        private String name;

        /**
         * Values will be stored lowercase by default, set isCaseInsensitive to
         * true if you want to keep the case of the value
         */
        @XmlAttribute
        private boolean isCaseInsensitive = false;

        /**
         * By default multiple values that are found for a matching pattern will
         * be stored. For some facets this leads to too much values with little
         * value for instance for "subject". Set allowMultipleValues to false
         * will only store the first found value.
         */
        @XmlAttribute
        private boolean allowMultipleValues = true;

        @XmlElement(name = "concept")
        private Collection<String> concepts = new ArrayList<String>();

        @XmlElement(name = "acceptableContext")
        private AcceptableContext acceptableContext;

        @XmlElement(name = "rejectableContext")
        private RejectableContext rejectableContext;

        @XmlElement(name = "pattern")
        private Collection<String> patterns = new ArrayList<String>();

        @XmlElement(name = "blacklistPattern")
        private Collection<String> blacklistPatterns = new ArrayList<String>();

        @XmlElement(name = "derivedFacet")
        private Collection<String> derivedFacets = new ArrayList<String>();

        public void setConcepts(Collection<String> concepts) {
            this.concepts = concepts;
        }

        public Collection<String> getConcepts() {
            return concepts;
        }

        public void setAccebtableContext(AcceptableContext context) {
            this.acceptableContext = context;
        }

        public AcceptableContext getAcceptableContext() {
            return acceptableContext;
        }

        public boolean hasAcceptableContext() {
            return (acceptableContext != null);
        }

        public void setRejectableContext(RejectableContext context) {
            this.rejectableContext = context;
        }

        public RejectableContext getRejectableContext() {
            return rejectableContext;
        }

        public boolean hasRejectableContext() {
            return (rejectableContext != null);
        }

        public boolean hasContext() {
            return (hasAcceptableContext() || hasRejectableContext());
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setCaseInsensitive(boolean isCaseInsensitive) {
            this.isCaseInsensitive = isCaseInsensitive;
        }

        public boolean isCaseInsensitive() {
            return isCaseInsensitive;
        }

        public void setAllowMultipleValues(boolean allowMultipleValues) {
            this.allowMultipleValues = allowMultipleValues;
        }

        public boolean isAllowMultipleValues() {
            return allowMultipleValues;
        }

        public void setPatterns(Collection<String> patterns) {
            this.patterns = patterns;
        }

        public Collection<String> getPatterns() {
            return patterns;
        }

        public void setBlacklistPatterns(Collection<String> blacklistPatterns) {
            this.blacklistPatterns = blacklistPatterns;
        }

        public Collection<String> getBlacklistPatterns() {
            return blacklistPatterns;
        }

        public Collection<String> getDerivedFacets() {
            return derivedFacets;
        }

        public void setDerivedFacets(Collection<String> derivedFacets) {
            this.derivedFacets = derivedFacets;
        }

        @Override
        public String toString() {
            return "name=" + name + ", patterns=" + patterns + ", concepts=" + concepts;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "acceptableContext")
    public static class AcceptableContext {

        @XmlAttribute
        private boolean includeAny = false;

        @XmlAttribute
        private boolean includeEmpty = true;

        @XmlElement(name = "concept")
        private Collection<String> concepts = new ArrayList<String>();

        public void setConcepts(Collection<String> concepts) {
            this.concepts = concepts;
        }

        public Collection<String> getConcepts() {
            return concepts;
        }

        public void setIncludeAny(boolean includeAny) {
            this.includeAny = includeAny;
        }

        public boolean includeAny() {
            return includeAny;
        }

        public void setIncludeEmpty(boolean includeEmpty) {
            this.includeEmpty = includeEmpty;
        }

        public boolean includeEmpty() {
            return includeEmpty;
        }

    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlRootElement(name = "rejectableContext")
    public static class RejectableContext {

        @XmlAttribute
        private boolean includeAny = true;

        @XmlAttribute
        private boolean includeEmpty = false;

        @XmlElement(name = "concept")
        private Collection<String> concepts = new ArrayList<String>();

        public void setConcepts(Collection<String> concepts) {
            this.concepts = concepts;
        }

        public Collection<String> getConcepts() {
            return concepts;
        }

        public void setIncludeAny(boolean includeAny) {
            this.includeAny = includeAny;
        }

        public boolean includeAny() {
            return includeAny;
        }

        public void setIncludeEmpty(boolean includeEmpty) {
            this.includeEmpty = includeEmpty;
        }

        public boolean includeEmpty() {
            return includeEmpty;
        }

    }

}

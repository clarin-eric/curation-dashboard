package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.facets.FacetConceptMappingService;
import eu.clarin.cmdi.curation.facets.Profile2FacetMap;
import eu.clarin.cmdi.curation.facets.Profile2FacetMap.Facet;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.FacetReport.FacetStruct;
import eu.clarin.cmdi.curation.report.FacetReport.FacetValue;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.xml.CMDXPathService;

/**
 * @author dostojic
 *
 */
public class InstanceFacetProcessor extends CMDSubprocessor {

	private static final String DEFAULT_LANGUAGE = "code:und";
	private static final String FACET_LANGUAGECODE = "languageCode";

	int numOfFacetsCoveredByIns = 0;
	
	private VTDNav navigator;

	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {
				
		FacetConceptMappingService facetMappingService = new FacetConceptMappingService();		
		Profile2FacetMap facetMappings;
		try{
			facetMappings = facetMappingService.getFacetMapping(report.header);
		
		
			report.facets = new FacetReportCreator().createFacetReport(facetMappings.getMappings());		
			
			//parse instance
			CMDXPathService xmlService = new CMDXPathService(entity.getPath());
			navigator = xmlService.getNavigator();
			extractFacetValues(facetMappings.getMappings(), report.facets.facets);
			
			report.facets.coveredByInstance = numOfFacetsCoveredByIns;
			report.facets.instanceCoverage = (double) numOfFacetsCoveredByIns / report.facets.numOfFacets;
		
		}catch (Exception e) {
			throw new Exception("Unable to obtain mapping for " + entity, e);
		};

	}
	
	@Override
	public Score calculateScore(CMDInstanceReport report) {
		return new Score(report.facets.instanceCoverage, 1.0, "facet-mapping", msgs);
	}


	private void extractFacetValues(Map<String, Facet> map, Collection<FacetStruct> facets) throws VTDException {
		for(FacetStruct facetStruct: facets){
			if(!facetStruct.covered)
				continue;
			//dont extract all values for collection mode
			if(facetStruct.name.equals("text") && Configuration.COLLECTION_MODE){
				continue;
			}
			
			Facet facet = map.get(facetStruct.name);
			
			boolean matchedPattern = false;
			Map<String, String> patterns = facet.getPatterns();
			for (String pattern : patterns.keySet()) {
				matchedPattern = matchPattern(facetStruct.name, pattern, patterns.get(pattern), facet.getAllowMultipleValues(), facetStruct);
				if (matchedPattern && !facet.getAllowMultipleValues()) {
					break;
				}
			}

			// using fallback patterns if extraction failed
			if (matchedPattern == false) {
				for (String pattern : facet.getFallbackPatterns()) {
					matchedPattern = matchPattern(facetStruct.name, pattern, null, facet.getAllowMultipleValues(), facetStruct);
					if (matchedPattern && !facet.getAllowMultipleValues()) {
						break;
					}
				}
			}
			
			
		}
	}

	/**
	 * Extracts content from CMDI file for a specific facet based on a single
	 * XPath expression
	 *
	 * @param cmdiData
	 *            representation of the CMDI document
	 * @param nav
	 *            VTD Navigator
	 * @param config
	 *            facet configuration
	 * @param pattern
	 *            XPath expression
	 * @param allowMultipleValues
	 *            information if multiple values are allowed in this facet
	 * @return pattern matched a node in the CMDI file?
	 * @throws VTDException
	 */
	private boolean matchPattern(String facet, String pattern, String concept, Boolean allowMultipleValues, FacetStruct facetStruct) throws VTDException {
		final AutoPilot ap = new AutoPilot(navigator);
		ap.declareXPathNameSpace("c", "http://www.clarin.eu/cmd/");
		ap.selectXPath(pattern);

		boolean matchedPattern = false;
		int index = ap.evalXPath();

		ArrayList<FacetValue> values = new ArrayList<>();

		while (index != -1) {
			matchedPattern = true;
			if (navigator.getTokenType(index) == VTDNav.TOKEN_ATTR_NAME) {
				// if it is an attribute you need to add 1 to the index to get
				// the right value
				index++;
			}
			final String value = navigator.toString(index);
			final String languageCode = extractLanguageCode(navigator);

			// ignore non-English language names for facet LANGUAGE_CODE
			if (facet.equals(FACET_LANGUAGECODE) && !languageCode.equals("code:eng")
					&& !languageCode.equals("code:und")) {
				index = ap.evalXPath();
				continue;
			}

			if (value != null && !value.isEmpty()){
				FacetValue newValue = new FacetValue(concept, pattern, value);
				if(!values.contains(newValue))
					values.add(newValue);
			}

			if (!allowMultipleValues)
				break;
			index = ap.evalXPath();
		}

		if (!values.isEmpty()){
			if(facetStruct.values == null){
				facetStruct.values = values;
				numOfFacetsCoveredByIns++;
			}else
				facetStruct.values.addAll(values);
			
		}
		
		
		return matchedPattern;
	}

	private String extractLanguageCode(VTDNav nav) throws NavException {
		// extract language code in xml:lang if available
		Integer langAttrIndex = nav.getAttrVal("xml:lang");
		String languageCode;
		if (langAttrIndex != -1) {
			languageCode = nav.toString(langAttrIndex).trim();
		} else {
			return DEFAULT_LANGUAGE;
		}

		return languageCode;
	}

}

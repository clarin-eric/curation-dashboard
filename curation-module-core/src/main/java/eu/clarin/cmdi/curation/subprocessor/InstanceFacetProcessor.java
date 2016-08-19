package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.ComparisonChain;
import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.facets.FacetConceptMappingService;
import eu.clarin.cmdi.curation.facets.FacetConstants;
import eu.clarin.cmdi.curation.facets.Profile2FacetMap;
import eu.clarin.cmdi.curation.facets.Profile2FacetMap.Facet;
import eu.clarin.cmdi.curation.facets.postprocessor.AvailabilityPostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.ContinentNamePostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.CountryNamePostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.LanguageCodePostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.LanguageNamePostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.LicensePostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.NationalProjectPostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.OrganisationPostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.PostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.ResourceClassPostProcessor;
import eu.clarin.cmdi.curation.facets.postprocessor.TemporalCoveragePostProcessor;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.FacetReport.FacetStruct;
import eu.clarin.cmdi.curation.report.FacetReport.FacetValue;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.xml.CMDXPathService;

/**
 * @author dostojic
 *
 */
public class InstanceFacetProcessor extends CMDSubprocessor {

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
			
			
			Collections.sort((List<FacetStruct>)report.facets.facets, (a, b) -> 
				 ComparisonChain.start()
				 	.compareTrueFirst(a.values != null, b.values != null)
					.compareTrueFirst(a.covered, b.covered)
					.result()
			);
			
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
		Collection<FacetStruct> derivedFacets= new ArrayList<>();
		for(FacetStruct facetStruct: facets){
			if(!facetStruct.covered)
				continue;
			//dont extract all values for collection mode
			if(facetStruct.name.equals(FacetConstants.FIELD_TEXT) && Configuration.COLLECTION_MODE){
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
			
			//normalize facet values
			normalise(facetStruct);
			
			
			//create derived facets and assigned values
			facet.getDerivedFacets().forEach(df -> {
				FacetStruct derivedFacet = new FacetStruct();
				derivedFacet.name = df;
				derivedFacet.derived = true;
				derivedFacet.covered = facetStruct.covered;
				derivedFacet.values = facetStruct.values
						.stream()
						//facetvalue is set to normalised facet value
						.map(facVal -> new FacetValue(null, null, facVal.normalisedValue))
						.collect(Collectors.toList());
				//normalize values for derived facet
				normalise(derivedFacet);
				
				derivedFacets.add(derivedFacet);
			});
		}
		
		//add derived facets to the report
		facets.addAll(derivedFacets);
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
			if (facet.equals(FacetConstants.FIELD_LANGUAGE_CODE) 
					&& !languageCode.equals(FacetConstants.ENG_LANGUAGE)
					&& !languageCode.equals(FacetConstants.DEFAULT_LANGUAGE)
				) {
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
			return FacetConstants.DEFAULT_LANGUAGE;
		}

		return languageCode;
	}
	
	private void normalise(FacetStruct facet){
		//don't normalise when assessing collection
		if(Configuration.COLLECTION_MODE || facet.values == null)
			return;
		PostProcessor postProcessor = getPostProcessor(facet.name);
		if(postProcessor == null)
			return;
		
		facet.values.forEach(item ->{
			String normalisedValue = String.join("; ", postProcessor.process(item.value));
			if(!item.value.equals(normalisedValue)){
				item.normalisedValue = normalisedValue;
				if(!normalisedValue.equals("--"))
					addMessage(Severity.INFO, "Normalised value for facet "+ facet.name + ": '" + item.value + "' into '" + normalisedValue + "'");
			}
		});
		
		//ignore values normalized to '--' for license facet
		if(facet.name.equals(FacetConstants.FIELD_LICENSE))
			facet.values.removeIf(item -> {
				if(item.normalisedValue != null && item.normalisedValue.equals("--")){
					addMessage(Severity.INFO, "Ignored value for facet "+ facet.name + ": '" + item.value + "'. This value will be removed from mapping");
					return true;
				}else
					return false;
				
			});		
	}
	
	
	private PostProcessor getPostProcessor(String facetName){
		switch (facetName) {
			//case FacetConstants.FIELD_ID: return new IdPostProcessor();
			case FacetConstants.FIELD_CONTINENT:
				return new ContinentNamePostProcessor();
			case FacetConstants.FIELD_COUNTRY:
				return new CountryNamePostProcessor();
			case FacetConstants.FIELD_LANGUAGE_CODE:
				return new LanguageCodePostProcessor();
			case FacetConstants.FIELD_LANGUAGE_NAME:
				return new LanguageNamePostProcessor();
			case FacetConstants.FIELD_AVAILABILITY:
				return new AvailabilityPostProcessor();
			case FacetConstants.FIELD_ORGANISATION:
				return new OrganisationPostProcessor();
			case FacetConstants.FIELD_TEMPORAL_COVERAGE:
				return new TemporalCoveragePostProcessor();
			case FacetConstants.FIELD_NATIONAL_PROJECT:
				return new NationalProjectPostProcessor();
			//case FacetConstants.FIELD_CLARIN_PROFILE: return new CMDIComponentProfileNamePostProcessor();
			case FacetConstants.FIELD_RESOURCE_CLASS:
				return new ResourceClassPostProcessor();
			case FacetConstants.FIELD_LICENSE:
				return new LicensePostProcessor();
			default:
				return null;
		}
	}

}

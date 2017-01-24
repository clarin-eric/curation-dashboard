package eu.clarin.cmdi.curation.subprocessor;

import java.util.Collection;
import java.util.Map;

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
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.FacetReport;
import eu.clarin.cmdi.curation.report.FacetReport.Coverage;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.xml.CMDXPathService;


public class CollectionInstanceFacetProcessor extends CMDSubprocessor {
	
	int numOfFacetsCoveredByIns = 0;
	
	private VTDNav navigator;

	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {
		
		entity.setParsedInstance(null);
		
		FacetConceptMappingService facetMappingService = new FacetConceptMappingService();		
		Profile2FacetMap facetMappings;
		try{
			facetMappings = facetMappingService.getFacetMapping(report.header);
		
		
			report.facets = new FacetReportCreator().createFacetReport(facetMappings.getMappings());	
			
			//parse instance
			CMDXPathService xmlService = new CMDXPathService(entity.getPath());
			navigator = xmlService.getNavigator();
			extractFacetValues(facetMappings.getMappings(), report.facets);
			
			double numOfCoveredByIns = report.facets.coverage.stream().filter(facet -> facet.coveredByInstance).count();
			report.facets.instanceCoverage = numOfCoveredByIns / report.facets.numOfFacets;
		
		}catch (Exception e) {
			throw new Exception("Unable to obtain mapping for " + entity, e);
		};

	}
	
	@Override
	public Score calculateScore(CMDInstanceReport report) {
		return new Score(report.facets.instanceCoverage, 1.0, "facet-mapping", msgs);
	}


	private void extractFacetValues(Map<String, Facet> facetMappings, FacetReport facetReport) throws VTDException {
		for(Coverage coverage: facetReport.coverage){
			if(!coverage.coveredByProfile)
				continue;
			
			String facetName = coverage.name;			
			Facet facet = facetMappings.get(facetName);
			
			boolean matchedPattern = false;
			Collection<String> patterns = facet.getPatterns();
			for (String pattern : patterns) {
				matchedPattern = matchPattern(facetName, pattern, facet.getAllowMultipleValues(), facetReport);
				if (matchedPattern && !facet.getAllowMultipleValues()) {
					break;
				}
			}

			// using fallback patterns if extraction failed
			if (matchedPattern == false) {
				for (String pattern : facet.getFallbackPatterns()) {
					matchedPattern = matchPattern(facetName, pattern, facet.getAllowMultipleValues(), facetReport);
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
	private boolean matchPattern(String facet, String pattern, Boolean allowMultipleValues, FacetReport facetReport) throws VTDException {
		final AutoPilot ap = new AutoPilot(navigator);
		ap.declareXPathNameSpace("c", "http://www.clarin.eu/cmd/");
		ap.selectXPath(pattern);

		boolean matchedPattern = false;
		int index = ap.evalXPath();

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

			if (value != null && !value.isEmpty()){ //&& normalize(facet, value) //skip normalisation
				facetReport.coverage.stream().filter(f -> f.name.equals(facet)).findFirst().ifPresent(f -> f.coveredByInstance = true);

				//in collection assessment we don't care about values, break when the first value is found and accepted
				break;
				
			}
			
			index = ap.evalXPath();
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
	
	/*
	 * returns boolean which tells if facet has to be accepted
	 * 
	 */
	private boolean normalize(String facetName, String value){
		
		PostProcessor postProcessor = getPostProcessor(facetName);
		if(postProcessor == null)
			return true;
		
		String normalisedValue = String.join("; ", postProcessor.process(value));
		//value '--' for facet license means that value is ignored
		return (facetName.equals(FacetConstants.FIELD_LICENSE) && normalisedValue.equals("--"))? false : true;
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

package eu.clarin.cmdi.curation.subprocessor;

import java.util.Collection;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.entities.CMDInstance;

import eu.clarin.cmdi.curation.facets.FacetConstants;
import eu.clarin.cmdi.curation.facets.FacetMappingCacheFactory;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.FacetReport;
import eu.clarin.cmdi.curation.report.FacetReport.Coverage;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.xml.CMDXPathService;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.mapping.FacetConfiguration;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMapping;


public class CollectionInstanceFacetProcessor extends CMDSubprocessor {
	
	int numOfFacetsCoveredByIns = 0;
	
	private VTDNav navigator;

	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {

		entity.setParsedInstance(null);
		
	
		FacetMapping facetMapping;
		try{
			facetMapping = FacetMappingCacheFactory.getInstance().getFacetMapping(report.header);
		
		
			report.facets = new FacetReportCreator().createFacetReport(facetMapping);	
			
			//parse instance
			CMDXPathService xmlService = new CMDXPathService(entity.getPath());
			navigator = xmlService.getNavigator();
			extractFacetValues(facetMapping, report.facets);
			
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


	private void extractFacetValues(FacetMapping facetMapping, FacetReport facetReport) throws VTDException {
		for(Coverage coverage: facetReport.coverage){
			if(!coverage.coveredByProfile)
				continue;
			
			String facetName = coverage.name;			
			FacetConfiguration facetConfig = facetMapping.getFacetConfiguration(facetName);
			
			boolean matchedPattern = false;
			Collection<Pattern> patterns = facetConfig.getPatterns();
			for (Pattern pattern : patterns) {
				matchedPattern = matchPattern(facetConfig, pattern.getPattern(), facetReport);
				if (matchedPattern && !facetConfig.getAllowMultipleValues()) {
					break;
				}
			}

			// using fallback patterns if extraction failed
			if (matchedPattern == false) {
				for (Pattern pattern : facetConfig.getFallbackPatterns()) {
					matchedPattern = matchPattern(facetConfig, pattern.getPattern(), facetReport);
					if (matchedPattern && !facetConfig.getAllowMultipleValues()) {
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
	private boolean matchPattern(FacetConfiguration facetConfig, String pattern, FacetReport facetReport) throws VTDException {
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
			if (facetConfig.getName().equals(FacetConstants.FIELD_LANGUAGE_CODE) 
					&& !languageCode.equals(FacetConstants.ENG_LANGUAGE)
					&& !languageCode.equals(FacetConstants.DEFAULT_LANGUAGE)
				) {
				index = ap.evalXPath();
				continue;
			}

			if (value != null && !value.isEmpty()){ //&& normalize(facet, value) //skip normalisation
				facetReport.coverage.stream().filter(f -> f.name.equals(facetConfig.getName())).findFirst().ifPresent(f -> f.coveredByInstance = true);

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
}

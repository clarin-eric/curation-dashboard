package eu.clarin.cmdi.curation.subprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.facets.FacetConstants;
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
import eu.clarin.cmdi.curation.report.FacetReport.FacetValue;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *
 */
public class InstanceFacetNormalizationProcessor extends CMDSubprocessor {

	Map<String, PostProcessor> postProcessors = new HashMap<>();

	@Override
	public void process(CMDInstance entity, CMDInstanceReport report) throws Exception {
		initPostProcessors();

		// normalize
		report.facets.facets.forEach(facetStruct ->{
			List<FacetValue> toBeRemoved = new ArrayList();
			if(facetStruct.values != null)				
				facetStruct.values.forEach(facetValue -> {
					String normalisedValue = String.join("; ", postProcess(facetStruct.name, facetValue.value));
					if(normalisedValue.equals("--")){//for some reason we are ignoring -- normalised value (availability / license facet)
						toBeRemoved.add(facetValue);
						return;
					}
					if(!facetValue.value.equals(normalisedValue)){
						facetValue.normalisedValue = normalisedValue;
						addMessage(Severity.WARNING, "Value '" + facetValue.value + "' for facet '" + facetStruct.name + "' is normalised to '" + normalisedValue + "'");
					}
				
				});
			if(facetStruct.values != null){
				toBeRemoved.forEach(fv -> {
					addMessage(Severity.WARNING, "value '" + fv.value + "' is ignored for facet '" + facetStruct.name + "'");
				});
				facetStruct.values.remove(toBeRemoved);
			}
		});

	}
	
	@Override
	public Score calculateScore(CMDInstanceReport report) {
		return new Score(1.0, 1.0, "facet-normalisation", msgs);
	}
	
	private List<String> postProcess(String facetName, String extractedValue) {
        List<String> resultList = new ArrayList<String>();
        if (postProcessors.containsKey(facetName)) {
            PostProcessor processor = postProcessors.get(facetName);
            resultList = processor.process(extractedValue);
        } else {
            resultList.add(extractedValue);
        }
        return resultList;
    }

	private void initPostProcessors() {
		//postProcessors.put(FacetConstants.FIELD_ID, new IdPostProcessor());
		postProcessors.put(FacetConstants.FIELD_CONTINENT, new ContinentNamePostProcessor());
		postProcessors.put(FacetConstants.FIELD_COUNTRY, new CountryNamePostProcessor());
		postProcessors.put(FacetConstants.FIELD_LANGUAGE_CODE, new LanguageCodePostProcessor());
		postProcessors.put(FacetConstants.FIELD_LANGUAGE_NAME, new LanguageNamePostProcessor());
		postProcessors.put(FacetConstants.FIELD_AVAILABILITY, new AvailabilityPostProcessor());
		postProcessors.put(FacetConstants.FIELD_ORGANISATION, new OrganisationPostProcessor());
		postProcessors.put(FacetConstants.FIELD_TEMPORAL_COVERAGE, new TemporalCoveragePostProcessor());
		postProcessors.put(FacetConstants.FIELD_NATIONAL_PROJECT, new NationalProjectPostProcessor());
		//postProcessors.put(FacetConstants.FIELD_CLARIN_PROFILE, new CMDIComponentProfileNamePostProcessor()); //don't use this one
		postProcessors.put(FacetConstants.FIELD_RESOURCE_CLASS, new ResourceClassPostProcessor());
		postProcessors.put(FacetConstants.FIELD_LICENSE, new LicensePostProcessor());
	}

}

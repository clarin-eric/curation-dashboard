package eu.clarin.cmdi.curation.facets.postprocessor;

import java.util.Arrays;
import java.util.List;

import eu.clarin.cmdi.curation.facets.postprocessor.utils.VLOConfigFactory;

public class LicensePostProcessor extends PostProcessorsWithControlledVocabulary{

	@Override
	public List<String> process(String value) {
		return Arrays.asList(normalize(value, "--"));
	}

	@Override
	public String getVocabularyName() {
		return VLOConfigFactory.getVloConfig().getLicenseURIMapUrl();
	}
}

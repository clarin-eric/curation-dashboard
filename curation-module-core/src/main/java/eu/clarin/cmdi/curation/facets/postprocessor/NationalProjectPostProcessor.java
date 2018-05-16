package eu.clarin.cmdi.curation.facets.postprocessor;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.facets.postprocessor.utils.VLOConfigFactory;

/**
 * Adds information about the affiliation of a metadata file to a national
 * project (like CLARIN-X etc.) into facet nationalProject
 *
 * @author Thomas Eckart
 *
 */
public class NationalProjectPostProcessor extends PostProcessorsWithControlledVocabulary {

    private final static Logger LOG = LoggerFactory.getLogger(NationalProjectPostProcessor.class);

    /**
     * Returns the national project based on the mapping in
     * Configuration.getNationalProjectMapUrl() If no mapping was found empty
     * String is returned
     *
     * @return
     */
    @Override
    public List<String> process(String value) {
    	return Arrays.asList(normalize(value.trim(), ""));
    }
    

	@Override
	public String getVocabularyName() {
        return VLOConfigFactory.getVloConfig().getNationalProjectMapping();
	}
    
}

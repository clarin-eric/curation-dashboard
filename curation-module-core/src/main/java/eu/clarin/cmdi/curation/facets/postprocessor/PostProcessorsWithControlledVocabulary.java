package eu.clarin.cmdi.curation.facets.postprocessor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.facets.controlled_vocabulary.ControlledVocabularyFactory;
import eu.clarin.cmdi.curation.facets.controlled_vocabulary.ControlledVocabularyService;

/* 
 * abstract class that encapsulates common map creation from mapping files
 * for some postprocessors like LanguageCodePostProcessor*
 * 
 * brings one more level in class hierarchy between interface PostPorcessor and concrete implementations
 * 
 * @author dostojic
 * 
 */

public abstract class PostProcessorsWithControlledVocabulary implements PostProcessor, ControlledVocabularyService {

	private final static Logger logger = LoggerFactory.getLogger(PostProcessorsWithControlledVocabulary.class);
	
	private final static String MAPPING_FILES_LOCATION = "https://raw.githubusercontent.com/clarin-eric/VLO-mapping/master/uniform-maps/";
	private ControlledVocabularyService vocabulary;
	
	public PostProcessorsWithControlledVocabulary(){
		try {
			URL url = new URL(getVocabularyName().replace("${vloconfig.mappingFilesLocation}", MAPPING_FILES_LOCATION));
			vocabulary = ControlledVocabularyFactory.getControlledVocabulary(url);
		} catch (MalformedURLException e) {
			throw new RuntimeException("Malformed URL", e);
		}
	}
	
	public abstract String getVocabularyName(); 

	public String normalize(String value) {
		//all variant values are kept in lower case
		return vocabulary.normalize(value.toLowerCase());
	}

	public String normalize(String value, String fallBackValue) {
		String normalizedVals = normalize(value);
		return normalizedVals != null ? normalizedVals : fallBackValue;
	}

	public Map<String, String> getCrossMappings(String value) {
		return vocabulary.getCrossMappings(value);
	}

	// for debug
	public void printMap() {
		logger.info(vocabulary.toString());
			
	}
}

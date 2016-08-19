package eu.clarin.cmdi.curation.facets.controlled_vocabulary;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import eu.clarin.cmdi.curation.facets.controlled_vocabulary.ControlledVocabulary.VocabularyEntry;
import eu.clarin.cmdi.curation.facets.controlled_vocabulary.VariantsMap.Mapping;
import eu.clarin.cmdi.curation.facets.controlled_vocabulary.VariantsMap.Variant;

public class ControlledVocabularyFactory {
	
	private final static Logger _logger = LoggerFactory.getLogger(ControlledVocabularyFactory.class);

	private static LoadingCache<URL, ControlledVocabulary> vocabulariesCache = CacheBuilder.newBuilder().concurrencyLevel(4).build(new ControlledVocabularyCacheLoader());
	
	
	public static ControlledVocabularyService getControlledVocabulary(URL vocabularyURL){
		try {
			return vocabulariesCache.get(vocabularyURL);
		} catch (ExecutionException e) {
			throw new RuntimeException("Unable to create controlled vocabulary from " + vocabularyURL, e);
		}
	}
	
	
	
	static class ControlledVocabularyCacheLoader extends CacheLoader<URL, ControlledVocabulary>{

		@Override
		public ControlledVocabulary load(URL key) throws Exception {
			
			_logger.info("Creating new controlled vocabulary from " + key.toString());
			
			JAXBContext jc = JAXBContext.newInstance(VariantsMap.class);
	        Unmarshaller unmarshaller = jc.createUnmarshaller();
	        VariantsMap rawMap = (VariantsMap) unmarshaller.unmarshal(key.openStream());
			List<VocabularyEntry> listOfEntries = new ArrayList<VocabularyEntry>();
			boolean containsRegEx = false;
			
			for(Mapping m: rawMap.mappings)
				if(m.variants != null){
					String normalizedValue = m.normalizedVlalue.value;
					
					for(Variant v: m.variants){
						if(!containsVocabularyEntry(listOfEntries, v.value.trim().toLowerCase()))
							listOfEntries.add(new VocabularyEntry(v.value.trim().toLowerCase(), normalizedValue, v.isRegExp, v.crossMappings));
						if(v.isRegExp)
							containsRegEx = true;
					}
				}

			return new ControlledVocabulary(listOfEntries, containsRegEx);
			
		}
		
		boolean containsVocabularyEntry(List<VocabularyEntry> listOfEntries, String val){
			for(VocabularyEntry entry: listOfEntries)
				if(entry.originalVal.equals(val))
					return true;
			
			return false;
		}
	}
	
}

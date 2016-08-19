package eu.clarin.cmdi.curation.facets.controlled_vocabulary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import eu.clarin.cmdi.curation.facets.controlled_vocabulary.VariantsMap.CrossMapping;

class ControlledVocabulary implements ControlledVocabularyService {
	
	Map<String, VocabularyEntry> vocabularyMap = null;

	// false is default because it is a special case when patterns are used in maps
	boolean searchInRegExes = false; 

	ControlledVocabulary(List<VocabularyEntry> entries, boolean searchInRegExes) {
		this(entries);
		this.searchInRegExes = searchInRegExes;
	}

	ControlledVocabulary(List<VocabularyEntry> entries) {
		vocabularyMap = new HashMap<>();
		entries.forEach(e -> vocabularyMap.put(e.originalVal, e));
	}
	
	@Override
	public String normalize(String value) {
		VocabularyEntry hit = getEntry(value);
		return (hit != null) ? hit.normalizedValue : null;
	}

	@Override
	public Map<String, String> getCrossMappings(String value) {
		VocabularyEntry hit = getEntry(value);
		return (hit != null) ? hit.crossMap : null;
	}

	VocabularyEntry getEntry(String value) {
		VocabularyEntry hit = vocabularyMap.get(value);
		// no hit -> check in patterns if option set
		if (hit == null && searchInRegExes){
			hit = vocabularyMap.entrySet()
			.stream()
			.map(e -> e.getValue()).filter(e -> {
				return e.isRegEx && Pattern.compile(e.originalVal).matcher(value).find();	
			})
			.findFirst()
			.orElse(null);
		}
		
		return hit;
	}

	
	static class VocabularyEntry {
		
		String originalVal;
		String normalizedValue;
		boolean isRegEx;
		Map<String, String> crossMap;	
		
		public VocabularyEntry(String originalVal, String normalizedValues, boolean isRegEx, List<CrossMapping> crossMap) {
			this.originalVal = originalVal;
			this.normalizedValue = normalizedValues;
			this.isRegEx = isRegEx;
			this.crossMap = new HashMap<String, String>();
			if(crossMap != null)
				for(CrossMapping cm: crossMap){
					this.crossMap.put(cm.facet, cm.value);
				}
		}

		@Override
		public String toString() {
			return originalVal + " -> " + normalizedValue + ", isRegEx=" + isRegEx + ", " + crossMap.toString(); 
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Number of entries: " + vocabularyMap.size() + "\n");
		vocabularyMap.entrySet().forEach(e -> sb.append(e + "\n"));
		return sb.toString();
	}
	
}

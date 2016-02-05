package eu.clarin.cmdi.curation.subprocessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ximpleware.AutoPilot;
import com.ximpleware.NavException;
import com.ximpleware.ParseException;
import com.ximpleware.VTDException;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

import eu.clarin.cmdi.curation.entities.CMDIRecord;
import eu.clarin.cmdi.curation.facets.FacetConceptMappingService;
import eu.clarin.cmdi.curation.facets.Profile2FacetMap;
import eu.clarin.cmdi.curation.facets.Profile2FacetMap.Facet;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *
 */
public class CMDIFaceting implements ProcessingActivity<CMDIRecord> {

    private static final Logger _logger = LoggerFactory.getLogger(CMDIFaceting.class);

    private static final String DEFAULT_LANGUAGE = "code:und";
    private static final String FACET_LANGUAGECODE = "languageCode";

    private Map<String, List<String>> facetValues = new HashMap<>();

    private VTDNav navigator;

    Severity highest;

    @Override
    public Severity process(CMDIRecord entity) {
	try {
	    parse(entity.getPath());

	    // coverage of profile
	    FacetConceptMappingService facetService = FacetConceptMappingService.getInstance();
	    Profile2FacetMap map = facetService.getMapping(entity.getProfile());
	    extractFacetValues(map);
	    
	    entity.setNumOfFacets(facetService.getFacetConceptsNames().size());
	    entity.setNumOfCoveredFacetsProfile(map.getMappings().size());	    
	    entity.setFacetValues(facetValues);

	    return Severity.NONE;

	} catch (Exception e) {
	    entity.addDetail(Severity.FATAL, e.getMessage());
	    return Severity.FATAL;
	}
    }

    private void parse(Path cmdiRecord) throws Exception {
	VTDGen parser = new VTDGen();
	try {
	    parser.setDoc(Files.readAllBytes(cmdiRecord));
	    parser.parse(true);
	    navigator = parser.getNav();
	    parser = null;
	} catch (IOException | ParseException e) {
	    throw new Exception("Errors while parsing " + cmdiRecord, e);
	}

    }

    private void extractFacetValues(Profile2FacetMap map) throws VTDException {
	Collection<Facet> facetList = map.getMappings();
	for (Facet facet : facetList) {
	    boolean matchedPattern = false;
	    List<String> patterns = facet.getPatterns();
	    for (String pattern : patterns) {
		matchedPattern = matchPattern(facet, pattern, facet.getAllowMultipleValues());
		if (matchedPattern && !facet.getAllowMultipleValues()) {
		    break;
		}
	    }

	    // using fallback patterns if extraction failed
	    if (matchedPattern == false) {
		for (String pattern : facet.getFallbackPatterns()) {
		    matchedPattern = matchPattern(facet, pattern, facet.getAllowMultipleValues());
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
    private boolean matchPattern(Facet facet, String pattern, Boolean allowMultipleValues) throws VTDException {
	final AutoPilot ap = new AutoPilot(navigator);
	ap.declareXPathNameSpace("c", "http://www.clarin.eu/cmd/");
	ap.selectXPath(pattern);

	boolean matchedPattern = false;
	int index = ap.evalXPath();

	LinkedList<String> values = new LinkedList<>();

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
	    if (facet.getName().equals(FACET_LANGUAGECODE) && !languageCode.equals("code:eng")
		    && !languageCode.equals("code:und")) {
		index = ap.evalXPath();
		continue;
	    }

	    values.add(value);

	    if (!allowMultipleValues)
		break;
	    index = ap.evalXPath();
	}

	facetValues.put(facet.getName(), values);
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

package eu.clarin.cmdi.curation.facets;

import java.util.Collection;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.LoadingCache;

import eu.clarin.cmdi.curation.cr.ProfileHeader;
import eu.clarin.cmdi.curation.facets.FacetConcepts.FacetConcept;

/**
 * @author dostojic
 *
 */
public class FacetConceptMappingService implements IFacetConceptMappingService{

	static final Logger _logger = LoggerFactory.getLogger(FacetConceptMappingService.class);

	// move this to config
	public static final String FACET_CONCEPTS_URL = "https://raw.githubusercontent.com/clarin-eric/VLO/master/vlo-commons/src/main/resources/facetConcepts.xml";

	public static final Collection<FacetConcept> facetConcepts = FacetConcepts.createFacetConcept();

	private static final LoadingCache<ProfileHeader, Profile2FacetMap> facetMappingPublicCache = FacetMappingCacheFactory.createFacetMappingCache(true);
	private static final LoadingCache<ProfileHeader, Profile2FacetMap> facetMappingNonpublicCache = FacetMappingCacheFactory.createFacetMappingCache(false);
	

	@Override
	public Collection<String> getFacetNames() {
		return facetConcepts.stream().map(FacetConcept::getName).collect(Collectors.toList());
	}

	@Override
	public Profile2FacetMap getFacetMapping(ProfileHeader header) throws Exception {
		return (header.isPublic? facetMappingPublicCache : facetMappingNonpublicCache).get(header);
		
	}
}

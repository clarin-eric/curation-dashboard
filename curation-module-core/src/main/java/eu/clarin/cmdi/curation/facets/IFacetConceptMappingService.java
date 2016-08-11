package eu.clarin.cmdi.curation.facets;

import java.util.Collection;

import eu.clarin.cmdi.curation.cr.ProfileHeader;

public interface IFacetConceptMappingService {

	public Collection<String> getFacetNames();

	public Profile2FacetMap getFacetMapping(ProfileHeader header) throws Exception;

}

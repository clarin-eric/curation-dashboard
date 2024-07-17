package eu.clarin.cmdi.curation.api.cache;

import eu.clarin.cmdi.curation.api.vlo_extension.FacetsMappingCacheFactory;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileHeader;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;


@Component
public class FacetMappingCache {
   
   @Cacheable(value = "facetMappingCache", key = "#schemaLocation", sync = true)
   public FacetsMapping getFacetMapping(String schemaLocation, FacetsMappingCacheFactory fac) {
      
      return fac.createFacetsMapping(schemaLocation);
      
   }
}

package eu.clarin.cmdi.curation.api.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.vlo_extension.FacetsMappingCacheFactory;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;


@Component
public class FacetMappingCache {
   
   @Autowired
   CRService crService;
   
   @Cacheable(value = "publicFacetMappingCache", key = "#header.id")
   public FacetsMapping getPublicFacetMapping(ProfileHeader header, FacetsMappingCacheFactory fac) {
      
      return fac.createFacetsMapping(header);
      
   }   
   
   @Cacheable(value = "nonPublicFacetMappingCache")
   public FacetsMapping getNonPublicFacetMapping(ProfileHeader header, FacetsMappingCacheFactory fac) {
      
      return fac.createFacetsMapping(header);
      
   }  
}

package eu.clarin.cmdi.curation.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;


@Component
public class FacetMappingCache {
   
   @Autowired
   CRService crService;
   
   @Cacheable(value = "publicFacetMappingCache")
   public FacetsMapping getPublicFacetMapping(ProfileHeader header, FacetsMapping mapping) {
      
      return mapping;
      
   }   
   
   @Cacheable(value = "nonPublicFacetMappingCache")
   public FacetsMapping getNonPublicFacetMapping(ProfileHeader header, FacetsMapping mapping) {
      
      return mapping;
      
   }   
}

package eu.clarin.cmdi.curation.api.vlo_extension;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ximpleware.NavException;

import eu.clarin.cmdi.curation.api.cache.FacetMappingCache;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.mapping.*;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 */
@Service
@Slf4j
public class FacetsMappingCacheFactory extends FacetMappingFactory {

   private VloConfig vloConfig;

   private CRService crService;

   private FacetMappingCache cache;

   @Autowired
   public FacetsMappingCacheFactory(VloConfig vloConfig, CRService crService, FacetMappingCache cache) {

      super(vloConfig, new VLOMarshaller());
      this.vloConfig = vloConfig;
      this.crService = crService;
      this.cache = cache;
   }

   public FacetsMapping getFacetMapping(String profileId, Boolean useLocalXSDCache) {

      return getFacetsMapping(
            crService.createProfileHeader(vloConfig.getComponentRegistryProfileSchema(profileId), "1.x", false));

   }

   public synchronized FacetsMapping getFacetsMapping(ProfileHeader header) {
      
      return header.isPublic() ? cache.getPublicFacetMapping(header, this)
            : cache.getNonPublicFacetMapping(header, this);
   }

   public Map<String, List<Pattern>> createConceptLinkPathMapping(ProfileHeader header)
         throws NoProfileCacheEntryException {
      
      final Map<String, List<Pattern>> result = new HashMap<>();

      crService.getParsedProfile(header).getElementNodes().entrySet()
         .stream()
         .forEach(element -> {
            result.computeIfAbsent(element.getValue().concept.getUri(), k -> new ArrayList<Pattern>())
            .add(new Pattern(element.getKey()));            
         });

      return result;
   }

   public FacetsMapping createFacetsMapping(ProfileHeader header) {
      return new FacetsMappingExt(createMapping(new ConceptLinkPathMapper() {

         @Override
         public Map<String, List<Pattern>> createConceptLinkPathMapping() throws NavException {
            
            final Map<String, List<Pattern>> result = new HashMap<>();

            try {
               crService.getParsedProfile(header).getElementNodes().entrySet()
                  .stream()
                  .filter(element -> (element.getValue().concept != null))
                  .forEach(element -> {
                     result.computeIfAbsent(element.getValue().concept.getUri(), k -> new ArrayList<Pattern>())
                     .add(new Pattern(element.getKey()));
                  });
            }
            catch (NoProfileCacheEntryException e) {

               log.error("no ProfileCacheEntry object for profile id '{}'", header.getId());

            }
            return result;
         }

         @Override
         public String getXsd() {
            return header.getId();
         }

         @Override
         public Boolean useLocalXSDCache() {
            return true;
         }
      }));
   }
}

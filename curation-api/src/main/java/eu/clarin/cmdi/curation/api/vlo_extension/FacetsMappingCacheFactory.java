package eu.clarin.cmdi.curation.api.vlo_extension;

import eu.clarin.cmdi.curation.api.cache.FacetMappingCache;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.mapping.ConceptLinkPathMapper;
import eu.clarin.cmdi.vlo.importer.mapping.FacetMappingFactory;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Facets mapping cache factory.
 *
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 */
@Service
@Slf4j
public class FacetsMappingCacheFactory extends FacetMappingFactory {

   private final VloConfig vloConfig;

   private final CRService crService;

   private final FacetMappingCache cache;

   /**
    * Instantiates a new Facets mapping cache factory.
    *
    * @param vloConfig the vlo config
    * @param crService the cr service
    * @param cache     the cache
    */
   @Autowired
   public FacetsMappingCacheFactory(VloConfig vloConfig, CRService crService, FacetMappingCache cache) {

      super(vloConfig, new VLOMarshaller());
      this.vloConfig = vloConfig;
      this.crService = crService;
      this.cache = cache;
   }

   /**
    * Gets facet mapping.
    *
    * @param profileId        the profile id
    * @param useLocalXSDCache the use local xsd cache
    * @return the facet mapping
    */
   @Override
   public FacetsMapping getFacetMapping(String profileId, Boolean useLocalXSDCache) {

        return getFacetsMapping(vloConfig.getComponentRegistryProfileSchema(profileId));
    }

   /**
    * Gets facets mapping.
    *
    * @param schemaLocation the URL of the schema
    * @return the facets mapping
    */
   public FacetsMapping getFacetsMapping(String schemaLocation) {
      
      return cache.getFacetMapping(schemaLocation, this);
   }


   /**
    * Create facets mapping.
    *
    * @param schemaLocation the URL of the schema
    * @return the facets mapping
    */
   public FacetsMapping createFacetsMapping(String schemaLocation) {

      return new FacetsMappingExt(createMapping(new ConceptLinkPathMapper() {

         @Override
         public Map<String, List<Pattern>> createConceptLinkPathMapping() {
            
            final Map<String, List<Pattern>> result = new HashMap<>();

            try {
               crService.getParsedProfile(schemaLocation, true).xpathElementNode().entrySet()
                  .stream()
                  .filter(element -> (element.getValue().concept != null))
                  .forEach(element -> result.computeIfAbsent(element.getValue().concept.getUri(), k -> new ArrayList<>())
                  .add(new Pattern(element.getKey())));
            }
            catch (NoProfileCacheEntryException e) {

               log.error("no ProfileCacheEntry object for schemaLocation '{}'", schemaLocation);

            }
            catch (CRServiceStorageException | CCRServiceNotAvailableException e) {
                throw new RuntimeException(e);
            }

             return result;
         }

         @Override
         public String getXsd() {
            return schemaLocation;
         }

         @Override
         public Boolean useLocalXSDCache() {
            return true;
         }
      }));
   }
}

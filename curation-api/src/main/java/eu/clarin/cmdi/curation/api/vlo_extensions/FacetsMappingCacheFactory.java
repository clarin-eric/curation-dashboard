package eu.clarin.cmdi.curation.api.vlo_extensions;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ximpleware.NavException;

import eu.clarin.cmdi.curation.api.cache.FacetMappingCache;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.curation.cr.profile_parser.CMDINode;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.mapping.*;
import lombok.extern.slf4j.Slf4j;

/**
 * The FacetMappingCacheFactory extends the FacetMappingFactory for two reasons:
 * 1. To cache public and non-public profiles in two separate, configurable
 * caches 2. To assure the presence of some custom facets like schemaLocation,
 * which we need imperatively in the CMDIData object but which are not defined
 * in the standard facetConfig.xml file
 *
 * @author Wolfgang Walter SAUER (wowasa) &lt;wolfgang.sauer@oeaw.ac.at&gt;
 */
@Service
@Slf4j
public class FacetsMappingCacheFactory extends FacetMappingFactory {

   
   private VloConfig vloConfig; 

   private CRService crService;

   private FacetMappingCache cache;

//   private final LoadingCache<ProfileHeader, FacetsMapping> facetMappingPublicCache;
//   private final LoadingCache<ProfileHeader, FacetsMapping> facetMappingNonPublicCache;


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

   public FacetsMapping getFacetsMapping(ProfileHeader header) {
      return header.isPublic() ? cache.getPublicFacetMapping(header, createFacetsMapping(header))
            : cache.getNonPublicFacetMapping(header, createFacetsMapping(header));
   }

   public Map<String, List<Pattern>> createConceptLinkPathMapping(ProfileHeader header)
         throws NoProfileCacheEntryException {
      Map<String, List<Pattern>> result = new HashMap<>();

      Map<String, CMDINode> elements = crService.getParsedProfile(header).getElements();

      for (Map.Entry<String, CMDINode> element : elements.entrySet()) {
         result.computeIfAbsent(element.getValue().concept.uri, k -> new ArrayList<Pattern>())
               .add(new Pattern(element.getKey()));
      }

      return result;
   }

   private FacetsMapping createFacetsMapping(ProfileHeader header) {
      return new FacetsMappingExt(createMapping(new ConceptLinkPathMapper() {

         @Override
         public Map<String, List<Pattern>> createConceptLinkPathMapping() throws NavException {
            Map<String, List<Pattern>> result = new HashMap<>();

            Map<String, CMDINode> elements;

            try {
               elements = crService.getParsedProfile(header).getElements();

               for (Map.Entry<String, CMDINode> element : elements.entrySet()) {
                  if (element.getValue().concept != null)
                     result.computeIfAbsent(element.getValue().concept.uri, k -> new ArrayList<Pattern>())
                           .add(new Pattern(element.getKey()));
               }
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

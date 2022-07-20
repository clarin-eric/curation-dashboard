package eu.clarin.cmdi.curation.vlo_extensions;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ximpleware.NavException;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.ProfileHeader;
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
@Slf4j
public class FacetsMappingCacheFactory extends FacetMappingFactory {

   @Autowired
   private static VloConfig vloConf; 


   private final LoadingCache<ProfileHeader, FacetsMapping> facetMappingPublicCache;
   private final LoadingCache<ProfileHeader, FacetsMapping> facetMappingNonPublicCache;

   private static FacetsMappingCacheFactory _facetMappingCacheFactory;

   public static FacetsMappingCacheFactory getInstance() throws IOException {
      if (_facetMappingCacheFactory == null) {
         VLOMarshaller marshaller = new VLOMarshaller();
         _facetMappingCacheFactory = new FacetsMappingCacheFactory(vloConf, marshaller);
      }
      return _facetMappingCacheFactory;
   }

   private FacetsMappingCacheFactory(VloConfig vloConfig, VLOMarshaller marshaller) {
      super(vloConfig, marshaller);

      this.facetMappingPublicCache = CacheBuilder.newBuilder().concurrencyLevel(4).build(new FacetMappingCacheLoader());

      this.facetMappingNonPublicCache = CacheBuilder.newBuilder().concurrencyLevel(4)
            .expireAfterWrite(8, TimeUnit.HOURS)// keep non public profiles 8 hours in cache
            .build(new FacetMappingCacheLoader());
   }

   public FacetsMapping getFacetMapping(String profileId, Boolean useLocalXSDCache) {
      try {
         return getFacetsMapping(new CRService()
               .createProfileHeader(vloConf.getComponentRegistryProfileSchema(profileId), "1.x", false));
      }
      catch (ExecutionException ex) {
         log.error("error while attempting to get facetMap for profileId {}", profileId, ex);
      }
      return null;
   }

   public FacetsMapping getFacetsMapping(ProfileHeader header) throws ExecutionException {
      return header.isPublic() ? this.facetMappingPublicCache.get(header) : this.facetMappingNonPublicCache.get(header);
   }

   public Map<String, List<Pattern>> createConceptLinkPathMapping(ProfileHeader header) throws Exception {
      Map<String, List<Pattern>> result = new HashMap<>();

      Map<String, CMDINode> elements = new CRService().getParsedProfile(header).getElements();

      for (Map.Entry<String, CMDINode> element : elements.entrySet()) {
         result.computeIfAbsent(element.getValue().concept.uri, k -> new ArrayList<Pattern>())
               .add(new Pattern(element.getKey()));
      }

      return result;
   }

   private class FacetMappingCacheLoader extends CacheLoader<ProfileHeader, FacetsMapping> {
      @Override
      public FacetsMapping load(ProfileHeader header) {
         return new FacetsMappingExt(FacetsMappingCacheFactory.this.createMapping(new ConceptLinkPathMapper() {

            @Override
            public Map<String, List<Pattern>> createConceptLinkPathMapping() throws NavException {
               Map<String, List<Pattern>> result = new HashMap<>();

               Map<String, CMDINode> elements;

               try {
                  elements = new CRService().getParsedProfile(header).getElements();

                  for (Map.Entry<String, CMDINode> element : elements.entrySet()) {
                     if (element.getValue().concept != null)
                        result.computeIfAbsent(element.getValue().concept.uri, k -> new ArrayList<Pattern>())
                              .add(new Pattern(element.getKey()));
                  }

                  return result;
               }
               catch (ExecutionException e) {
                  throw new NavException(Arrays.toString(e.getCause().getStackTrace()));
               }

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
}

package eu.clarin.cmdi.curation.api.vlo_extension;

import eu.clarin.cmdi.curation.api.cache.FacetMappingCache;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.NoCRCacheEntryException;
import eu.clarin.cmdi.curation.cr.exception.PPHCacheException;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.api.FacetsMappingBuilder;
import eu.clarin.cmdi.vlo.importer.api.FacetsMappingDecorator;
import eu.clarin.cmdi.vlo.importer.api.FacetsMappingProvider;
import eu.clarin.cmdi.vlo.importer.mapping.FacetDefinition;
import eu.clarin.cmdi.vlo.importer.mapping.FacetsMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Supplies facets mappings for the curation module, cached by schema location.
 *
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 */
@Service
@Slf4j
public class FacetsMappingCacheFactory implements FacetsMappingProvider {

   private final FacetsMappingBuilder builder;

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

      this.builder = new FacetsMappingBuilder(vloConfig);
      this.crService = crService;
      this.cache = cache;
   }

   /**
    * Gets facets mapping.
    *
    * @param schemaLocation the URL of the schema
    * @return the facets mapping
    */
   @Override
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

      return new FacetsMappingDecorator(builder.build(schemaLocation, this::conceptLinkPaths), curationFacets());
   }

   /**
    * Concept URI to XPath mappings for a profile, read from the component registry.
    *
    * @param schemaLocation the URL of the schema
    * @return the patterns per concept URI
    */
   private Map<String, List<Pattern>> conceptLinkPaths(String schemaLocation) {

      final Map<String, List<Pattern>> result = new HashMap<>();

      try {
         crService.getParsedProfile(schemaLocation).xpathElementNode().entrySet()
            .stream()
            .filter(element -> (element.getValue().concept != null))
            .forEach(element -> result.computeIfAbsent(element.getValue().concept.getUri(), k -> new ArrayList<>())
            .add(new Pattern(element.getKey())));
      }
      catch (NoCRCacheEntryException e) {

         log.error("no ProfileCacheEntry object for schemaLocation '{}'", schemaLocation);

      }
      catch (CCRServiceNotAvailableException | PPHCacheException e) {
          throw new RuntimeException(e);
      }

      return result;
   }

   /**
    * Facets the curation module needs that VLO does not define in facetConcepts.xml.
    *
    * @return the additional facet definitions, by name
    */
   private static Map<String, FacetDefinition> curationFacets() {

      final LinkedHashMap<String, FacetDefinition> extra = new LinkedHashMap<>();

      extra.put("curation_schemaLocation",
            facet("curation_schemaLocation", "/cmd:CMD/@*[local-name()='schemaLocation']"));
      extra.put("curation_noNamespaceSchemaLocation",
            facet("curation_noNamespaceSchemaLocation", "/cmd:CMD/@*[local-name()='noNamespaceSchemaLocation']"));
      extra.put("curation_mdProfile",
            facet("curation_mdProfile", "/cmd:CMD/cmd:Header/cmd:MdProfile/text()"));

      return extra;
   }

   private static FacetDefinition facet(String name, String xpath) {

      final FacetDefinition definition = new FacetDefinition(null, name);
      definition.setPattern(new Pattern(xpath));
      return definition;
   }
}

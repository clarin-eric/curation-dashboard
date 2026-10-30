/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.conf;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.NoCRCacheEntryException;
import eu.clarin.cmdi.curation.cr.exception.PPHCacheException;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.api.CmdiRecordAnalyzer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Component
@Lazy
@Slf4j
public class ApiBeans {

   /**
    * Facets that VLO does not define in facetConcepts.xml but needed for header checks
    */
   private static final Map<String, String> CURATION_FACETS = new LinkedHashMap<>();

   static {
      CURATION_FACETS.put("curation_schemaLocation", "/cmd:CMD/@*[local-name()='schemaLocation']");
      CURATION_FACETS.put("curation_noNamespaceSchemaLocation", "/cmd:CMD/@*[local-name()='noNamespaceSchemaLocation']");
      CURATION_FACETS.put("curation_mdProfile", "/cmd:CMD/cmd:Header/cmd:MdProfile/text()");
   }

   private final VloConfig vloConfig;
   private final CRService crService;

   public ApiBeans(VloConfig vloConfig, CRService crService) {

      this.vloConfig = vloConfig;
      this.crService = crService;
   }

   /**
    * The analyzer that {@link eu.clarin.cmdi.curation.api.entity.CMDInstance}
    * runs each record through.
    *
    * <p>
    * Configured for analysis rather than import: every record keeps the full
    * ValueSet per facet value.
    *
    * </p>
    *
    * @return the analyzer
    */
   @Bean
   public CmdiRecordAnalyzer cmdiRecordAnalyzer() {

      return new CmdiRecordAnalyzer(vloConfig, this::conceptPaths, CURATION_FACETS);
   }

   /**
    * The XPaths of each concept link in a profile: concept URI to XPaths.
    *
    * @param schemaLocation the URL of the schema
    * @return the concept paths, empty if the profile is not in the CR cache
    */
   private Map<String, List<String>> conceptPaths(String schemaLocation) {

      final Map<String, List<String>> result = new HashMap<>();

      try {
         crService.getParsedProfile(schemaLocation).xpathElementNode().entrySet()
            .stream()
            .filter(element -> (element.getValue().concept != null))
            .forEach(element -> result.computeIfAbsent(element.getValue().concept.getUri(), k -> new ArrayList<>())
            .add(element.getKey()));
      }
      catch (NoCRCacheEntryException e) {

         log.error("no ProfileCacheEntry object for schemaLocation '{}'", schemaLocation);

      }
      catch (CCRServiceNotAvailableException | PPHCacheException e) {
          throw new RuntimeException(e);
      }

      return result;
   }
}

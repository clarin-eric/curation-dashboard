package eu.clarin.cmdi.curation.api.analysis;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.NoCRCacheEntryException;
import eu.clarin.cmdi.curation.cr.exception.PPHCacheException;
import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.CMDIDataFactory;
import eu.clarin.cmdi.vlo.importer.CMDIRecordProcessor;
import eu.clarin.cmdi.vlo.importer.Pattern;
import eu.clarin.cmdi.vlo.importer.api.ProcessingStrategy;
import eu.clarin.cmdi.vlo.importer.mapping.ConceptLinkPathMapper;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyzes every record, keeping the full ValueSet per facet value, with the
 * concept links of the profiles in the CR cache.
 */
@Slf4j
public class CurationProcessingStrategy implements ProcessingStrategy<Map<String, List<ValueSet>>> {

   /**
    * Facets that VLO does not define in facetConcepts.xml but needed for header checks
    */
   private static final Map<String, String> CURATION_FACETS = new LinkedHashMap<>();

   static {
      CURATION_FACETS.put("curation_schemaLocation", "/cmd:CMD/@*[local-name()='schemaLocation']");
      CURATION_FACETS.put("curation_noNamespaceSchemaLocation", "/cmd:CMD/@*[local-name()='noNamespaceSchemaLocation']");
      CURATION_FACETS.put("curation_mdProfile", "/cmd:CMD/cmd:Header/cmd:MdProfile/text()");
   }

   private final CRService crService;

   public CurationProcessingStrategy(CRService crService) {

      this.crService = crService;
   }

   @Override
   public CMDIDataFactory<Map<String, List<ValueSet>>> dataFactory(FieldNameService fieldNames) {

      return () -> new ValueSetCmdiData(fieldNames);
   }

   @Override
   public ConceptLinkPathMapper conceptLinkPathMapper(String schemaLocation) {

      return new ConceptLinkPathMapper() {

         @Override
         public Map<String, List<Pattern>> createConceptLinkPathMapping() {

            return conceptPaths(schemaLocation);
         }

         @Override
         public String getXsd() {

            return schemaLocation;
         }

         @Override
         public Boolean useLocalXSDCache() {

            // irrelevant: the concept links come from the CR cache, so no XSD is fetched
            return true;
         }
      };
   }

   @Override
   public Map<String, String> extraFacets() {

      return CURATION_FACETS;
   }

   @Override
   public CMDIRecordProcessor<Map<String, List<ValueSet>>> recordProcessor(
         CMDIDataProcessor<Map<String, List<ValueSet>>> parser, FieldNameService fieldNames) {

      return new CurationRecordProcessor(parser, fieldNames);
   }

   /**
    * The XPaths of each concept link in a profile: concept URI to XPaths.
    *
    * @param schemaLocation the URL of the schema
    * @return the concept paths, empty if the profile is not in the CR cache
    */
   private Map<String, List<Pattern>> conceptPaths(String schemaLocation) {

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
}

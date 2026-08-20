/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.conf;

import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.api.CmdiRecordAnalyzer;
import eu.clarin.cmdi.vlo.importer.api.FacetsMappingProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Lazy
public class ApiBeans {

   private final FacetsMappingProvider facetsMappingProvider;
   private final VloConfig vloConfig;

   public ApiBeans(FacetsMappingProvider facetsMappingProvider, VloConfig vloConfig) {

      this.facetsMappingProvider = facetsMappingProvider;
      this.vloConfig = vloConfig;
   }

   /**
    * The analyzer that {@link eu.clarin.cmdi.curation.api.entity.CMDInstance}
    * runs each record through.
    *
    * <p>
    * Configured for analysis rather than import: every record keeps the full
    * ValueSet per facet value.
    * {@link eu.clarin.cmdi.curation.api.vlo_extension.FacetsMappingCacheFactory}
    * describes the used facet mappings - and adds some extra curation facets
    * </p>
    *
    * @return the analyzer
    */
   @Bean
   public CmdiRecordAnalyzer cmdiRecordAnalyzer() {

      return CmdiRecordAnalyzer.create(vloConfig, facetsMappingProvider);
   }
}

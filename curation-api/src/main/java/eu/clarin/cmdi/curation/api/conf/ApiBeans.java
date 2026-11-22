/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.conf;

import eu.clarin.cmdi.curation.api.analysis.CurationProcessingStrategy;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.api.CmdiRecordAnalyzer;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 *
 */
@Component
@Lazy
public class ApiBeans {

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
    * @return the analyzer
    */
   @Bean
   public CmdiRecordAnalyzer<Map<String, List<ValueSet>>> cmdiRecordAnalyzer() {

      return new CmdiRecordAnalyzer<>(vloConfig, new CurationProcessingStrategy(crService));
   }
}

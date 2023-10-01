/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.conf;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.vlo_extension.CMDIDataImplFactory;
import eu.clarin.cmdi.curation.api.vlo_extension.FacetsMappingCacheFactory;
import eu.clarin.cmdi.vlo.LanguageCodeUtils;
import eu.clarin.cmdi.vlo.config.FieldNameServiceImpl;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.importer.CMDIRecordProcessor;
import eu.clarin.cmdi.vlo.importer.MetadataImporter;
import eu.clarin.cmdi.vlo.importer.VLOMarshaller;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.processor.CMDIParserVTDXML;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

/**
 *
 */
@Component
@Lazy
public class ApiBeans {
   @Autowired
   private FacetsMappingCacheFactory fac;
   @Autowired
   VloConfig vloConfig;
   
   @Bean
   public CMDIRecordProcessor<Map<String, List<ValueSet>>> cmdiRecordProcessor(){

      final LanguageCodeUtils languageCodeUtils = new LanguageCodeUtils(vloConfig);

      final FieldNameServiceImpl fieldNameServiceImpl = new FieldNameServiceImpl(vloConfig);

      final CMDIDataImplFactory cmdiDataFactory = new CMDIDataImplFactory(fieldNameServiceImpl);

      final VLOMarshaller marshaller = new VLOMarshaller();

      CMDIDataProcessor<Map<String, List<ValueSet>>> dataProcessor = new CMDIParserVTDXML<>(
            MetadataImporter.registerPostProcessors(vloConfig, fieldNameServiceImpl, languageCodeUtils),
            MetadataImporter.registerPostMappingFilters(fieldNameServiceImpl), vloConfig, fac, marshaller,
            cmdiDataFactory, fieldNameServiceImpl, false);

      return new CMDIRecordProcessor<Map<String, List<ValueSet>>>(dataProcessor, fieldNameServiceImpl) {

         @Override
         protected boolean skipOnDuplicateId() {

            return false;
         }

         @Override
         protected boolean skipOnNoResources() {

            return false;
         }
      };          
   }   

}

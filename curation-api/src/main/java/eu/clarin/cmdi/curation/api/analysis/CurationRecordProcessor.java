package eu.clarin.cmdi.curation.api.analysis;

import eu.clarin.cmdi.vlo.config.FieldNameService;
import eu.clarin.cmdi.vlo.importer.CMDIRecordProcessor;
import eu.clarin.cmdi.vlo.importer.processor.CMDIDataProcessor;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;

import java.util.List;
import java.util.Map;

/**
 * Curation reports on every record, so none is skipped.
 */
class CurationRecordProcessor extends CMDIRecordProcessor<Map<String, List<ValueSet>>> {

   CurationRecordProcessor(CMDIDataProcessor<Map<String, List<ValueSet>>> parser, FieldNameService fieldNames) {

      super(parser, fieldNames);
   }

   @Override
   protected boolean skipOnDuplicateId() {

      return false;
   }

   @Override
   protected boolean skipOnNoResources() {

      return false;
   }
}

package eu.clarin.cmdi.curation.api.entity;

import eu.clarin.cmdi.curation.api.processor.CMDInstanceProcessor;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.CMDIRecordProcessor;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import eu.clarin.cmdi.vlo.importer.solr.DocumentStoreException;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Scope(value="prototype")
@RequiredArgsConstructor
@Data
@Slf4j
public class CMDInstance {

    private final CMDInstanceProcessor processor;

    private final CMDIRecordProcessor<Map<String, List<ValueSet>>> cmdiRecordProcessor;

   private Optional<CMDIData<Map<String, List<ValueSet>>>> cmdiData;

   private Path path;
   private long size;
   private String url;
   
   private String providergroupName;
   
   public Optional<CMDIData<Map<String, List<ValueSet>>>> getCmdiData() {
      
      if(this.cmdiData == null) {
         
         try {
            
            this.cmdiData = cmdiRecordProcessor.processRecord(this.path.toFile());
         }
         catch (DocumentStoreException e) {

            log.error("can't create CMDIData instance of file '{}'", this.path);
         }
         catch (IOException e) {

            log.error("can't read file '{}'", this.path);
         }
      }
      return this.cmdiData;
   }

   public CMDInstanceReport generateReport() throws MalFunctioningProcessorException {
      
      return processor.process(this);
   }
}

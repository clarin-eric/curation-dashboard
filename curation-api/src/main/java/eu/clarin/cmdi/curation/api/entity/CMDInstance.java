package eu.clarin.cmdi.curation.api.entity;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.processor.CMDInstanceProcessor;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.CMDIRecordProcessor;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import eu.clarin.cmdi.vlo.importer.solr.DocumentStoreException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Scope(value="prototype")
@Data
@Slf4j
public class CMDInstance {

   private Optional<CMDIData<Map<String, List<ValueSet>>>> cmdiData;

   private Path path;
   private long size;
   private String url;
   
   private String providergroupName;
   
   @Autowired
   private CMDInstanceProcessor processor;
   @Autowired
   private CMDIRecordProcessor<Map<String, List<ValueSet>>> cmdiRecordProcessor;

   public CMDInstance(Path path) {
      
      this.path = path;

   }

   public CMDInstance(Path path, long size, String providergroupName) {
      
      this.path = path;
      this.size = size;
      this.providergroupName = providergroupName;
   
   }
   
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

   public CMDInstanceReport generateReport() {
      
      return processor.process(this);
   
   }
}

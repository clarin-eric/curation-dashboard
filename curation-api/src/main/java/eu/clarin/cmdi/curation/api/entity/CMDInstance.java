package eu.clarin.cmdi.curation.api.entity;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.instance_parser.ParsedInstance;
import eu.clarin.cmdi.curation.api.processor.CMDInstanceProcessor;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.Data;

@Component
@Scope(value="prototype")
@Data
public class CMDInstance {

   private ParsedInstance parsedInstance = null;

   private CMDIData<Map<String, List<ValueSet>>> cmdiData;

   private Path path;
   private long size;
   private String url;
   
   private String providergroupName;
   
   @Autowired
   private CMDInstanceProcessor processor;

   public CMDInstance(Path path) {
      
      this.path = path;

   }

   public CMDInstance(Path path, long size, String providergroupName) {
      
      this.path = path;
      this.size = size;
      this.providergroupName = providergroupName;
   
   }

   public CMDInstanceReport generateReport() {
      
      return processor.process(this);
   
   }
}

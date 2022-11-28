package eu.clarin.cmdi.curation.api.entity;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.instance_parser.ParsedInstance;
import eu.clarin.cmdi.curation.api.processor.CMDInstanceProcessor;
import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.vlo.importer.CMDIData;
import eu.clarin.cmdi.vlo.importer.processor.ValueSet;
import lombok.Data;

@Component
@Scope(value="prototype")
@Data
public class CMDInstance {

   public static Collection<String> mdSelfLinks = Collections.synchronizedCollection(new HashSet<>());
   public static Collection<String> duplicateMDSelfLink = Collections.synchronizedCollection(new HashSet<>());

   private ParsedInstance parsedInstance = null;

   private CMDIData<Map<String, List<ValueSet>>> cmdiData;

   private Path path = null;
   private long size = 0;
   private String url;
   
   @Autowired
   private CMDInstanceProcessor processor;

   public CMDInstance(Path path) {
      this.path = path;
   }

   public CMDInstance(Path path, long size) {
      this.path = path;
      this.size = size;
   }

   public CMDInstanceReport generateReport() {
      return processor.process(this);
   }
}

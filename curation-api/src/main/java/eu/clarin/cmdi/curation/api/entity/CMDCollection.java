package eu.clarin.cmdi.curation.api.entity;

import eu.clarin.cmdi.curation.api.processor.CMDCollectionProcessor;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Scope(value="prototype")
@Data
public class CMDCollection {

   @Autowired
   CMDCollectionProcessor processor;

   private Path path = null;

   public CMDCollection(Path path) {
      this.path = path;
   }

   public CollectionReport generateReport() {
      return processor.process(this);
   }
}

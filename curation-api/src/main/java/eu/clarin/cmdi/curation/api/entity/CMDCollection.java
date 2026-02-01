package eu.clarin.cmdi.curation.api.entity;

import eu.clarin.cmdi.curation.api.processor.CMDCollectionProcessor;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Scope(value="prototype")
@RequiredArgsConstructor
@Data
public class CMDCollection {

   private final CMDCollectionProcessor processor;

   private Path path;

   public CollectionReport generateReport() {
      return processor.process(this);
   }
}

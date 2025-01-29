package eu.clarin.cmdi.curation.api.processor;

import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.subprocessor.collection.CollectionAggregator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
@Scope("prototype")
public class CMDCollectionProcessor {
   
   @Autowired
   CollectionAggregator collectionAggregator;

   public CollectionReport process(CMDCollection collection) {

      LocalDateTime start = LocalDateTime.now();

      CollectionReport report = new CollectionReport();
      
      log.info("Started report generation for collection: " + collection.getPath());
      
      if(collection.getPath().getNameCount() >= 1) {
          
         report.fileReport.provider = collection.getPath().getName(collection.getPath().getNameCount() -1).toString();
         collectionAggregator.process(collection, report);
   
         LocalDateTime end = LocalDateTime.now();

         log.info("It took {} to generate the report for collection: {}", Duration.between(start, end).toString(), report.getName());
      }
      else {
         log.error("the provider group is the last name in the path. Therefore the collection path MUSTN'T be the root directory");
      }

      return report;
   }
}

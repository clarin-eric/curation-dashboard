package eu.clarin.cmdi.curation.api.processor;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.report.CollectionReport;
import eu.clarin.cmdi.curation.api.subprocessor.ext.CollectionAggregator;
import eu.clarin.cmdi.curation.api.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Scope("prototype")
public class CollectionProcessor {

   public CollectionReport process(CMDCollection collection) {

      long start = System.currentTimeMillis();

      CollectionReport report = new CollectionReport();
      log.info("Started report generation for collection: " + collection.getPath());

      CollectionAggregator collectionAggregator = null;
      try {
         collectionAggregator = new CollectionAggregator();

         collectionAggregator.process(collection, report);
         report.addSegmentScore(collectionAggregator.calculateScore(report));

      }
      catch (Exception e) {
         log.debug("Exception when processing " + collectionAggregator.getClass().toString() + " : " + e.getMessage());
         addInvalidFile(report, e);
      }

      long end = System.currentTimeMillis();
      log.info("It took " + TimeUtils.humanizeToTime(end - start) + " to generate the report for collection: "
            + report.getName());

      return report;
   }

   private void addInvalidFile(CollectionReport report, Exception e) {
      CollectionReport.InvalidFile invalidFile = new CollectionReport.InvalidFile();
      invalidFile.recordName = e.getMessage();
      invalidFile.reason = e.getCause() == null ? null : e.getCause().getMessage();
      report.addInvalidFile(invalidFile);
   }
}

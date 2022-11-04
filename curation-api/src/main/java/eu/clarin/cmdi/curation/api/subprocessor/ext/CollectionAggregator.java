package eu.clarin.cmdi.curation.api.subprocessor.ext;

import eu.clarin.cmdi.curation.api.configuration.CurationConfig;
import eu.clarin.cmdi.curation.api.entities.CMDCollection;
import eu.clarin.cmdi.curation.api.entities.CMDInstance;
import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.*;
import eu.clarin.cmdi.curation.api.report.CollectionReport.*;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractMessageCollection;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 */
@Slf4j
public class CollectionAggregator extends AbstractMessageCollection{

   @Autowired
   private CurationConfig conf;

   public void process(CMDCollection collection, CollectionReport report) {





      for (String facetName : conf.getFacets()) {
         FacetCollectionStruct facet = new FacetCollectionStruct();
         facet.name = facetName;
         facet.cnt = 0;
         report.facetReport.facet.add(facet);
      }

      // add info regarding file statistics
      report.fileReport.provider = collection.getPath().getFileName().toString();
      report.fileReport.numOfFiles = collection.getNumOfFiles();
      report.fileReport.size = collection.getSize();
      report.fileReport.minFileSize = collection.getMinFileSize();
      report.fileReport.maxFileSize = collection.getMaxFileSize();

      ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(conf.getThreadPoolSize());

      while (!collection.getChildren().isEmpty()) {

         CMDInstance instance = collection.getChildren().pop();
         executor.submit(() -> {
            try {
               CMDInstanceReport cmdInstanceReport = instance.generateReport(report.getName());
               cmdInstanceReport.mergeWithParent(report);
            }
            catch (SubprocessorException e) {

               log.debug("Error while generating report for instance: " + instance.getPath() + ":" + e.getMessage()
                     + " Skipping to next instance...");
               new ErrorReport(conf.getDirectory().getData().relativize(instance.getPath()).toString(), e.getMessage())
                     .mergeWithParent(report);
            }

            catch (Exception e) {
               
               log.error("", e);
            }
         });
      }

      executor.shutdown();

      while (!executor.isTerminated()) {
         try {
            Thread.sleep(1000);
         }
         catch (InterruptedException ex) {
            log.error("Error occured while waiting for the threadpool to terminate.");
         }
      }

      report.calculateAverageValues();

      if (!CMDInstance.duplicateMDSelfLink.isEmpty()) {
         report.headerReport.duplicatedMDSelfLink = CMDInstance.duplicateMDSelfLink;
      }
      CMDInstance.duplicateMDSelfLink.clear();
      CMDInstance.mdSelfLinks.clear();

   }

   public Score calculateScore(CollectionReport report) {
      double score = report.fileReport.numOfFiles;
      if (report.file != null) {
         report.file
               .forEach(ir -> addMessage(Severity.ERROR, "Invalid file:" + ir.recordName + ", reason: " + ir.reason));
         score = (score - report.file.size()) / score;
      }

      return new Score(score, (double) report.fileReport.numOfFiles, "invalid-files", this.getMessages());
   }

}

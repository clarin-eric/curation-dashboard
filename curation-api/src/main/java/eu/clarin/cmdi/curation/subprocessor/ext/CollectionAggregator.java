package eu.clarin.cmdi.curation.subprocessor.ext;

import eu.clarin.cmdi.curation.configuration.CurationConfig;
import eu.clarin.cmdi.curation.entities.CMDCollection;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.exception.SubprocessorException;
import eu.clarin.cmdi.curation.report.*;
import eu.clarin.cmdi.curation.report.CollectionReport.*;
import eu.clarin.cmdi.curation.report.CollectionReport.FacetReport;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 *
 */
@Slf4j
public class CollectionAggregator {

   @Autowired
   private CurationConfig conf;

   protected Collection<Message> msgs = null;

   public void process(CMDCollection collection, CollectionReport report) {

      report.fileReport = new FileReport();
      report.headerReport = new HeaderReport();
      report.resProxyReport = new ResProxyReport();
      report.xmlPopulatedReport = new XMLPopulatedReport();
      report.xmlValidationReport = new XMLValidationReport();
      report.urlReport = new URLValidationReport();
      report.facetReport = new FacetReport();

      report.facetReport.facet = new ArrayList<>();

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

      return new Score(score, (double) report.fileReport.numOfFiles, "invalid-files", msgs);
   }

   protected void addMessage(Severity lvl, String message) {
      if (msgs == null) {
         msgs = new ArrayList<>();
      }
      msgs.add(new Message(lvl, message));
   }

}

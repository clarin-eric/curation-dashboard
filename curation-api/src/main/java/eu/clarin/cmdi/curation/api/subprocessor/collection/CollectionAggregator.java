package eu.clarin.cmdi.curation.api.subprocessor.collection;

import eu.clarin.cmdi.cpa.model.AggregatedStatus;
import eu.clarin.cmdi.cpa.repository.AggregatedStatusRepository;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.*;
import eu.clarin.cmdi.curation.api.report.CollectionReport.*;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractMessageCollection;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Stream;



/**
 *
 */
@Slf4j
@Component
public class CollectionAggregator extends AbstractMessageCollection{

   @Autowired
   private ApiConfig conf;
   @Autowired
   private ApplicationContext ctx;
   @Autowired
   AggregatedStatusRepository aRep;

   @Transactional
   public void process(CMDCollection collection, CollectionReport report) throws IOException {
      
      for (String facetName : conf.getFacets()) {
         FacetCollectionStruct facet = new FacetCollectionStruct();
         facet.name = facetName;
         facet.cnt = 0;
         report.facetReport.facet.add(facet);
      }

      ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(conf.getThreadPoolSize());    

      Files.walkFileTree(collection.getPath(), new FileVisitor<Path>() {

         @Override
         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

            return FileVisitResult.CONTINUE;
         }

         @Override
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            
            report.fileReport.numOfFiles++;
            
            if(attrs.size() > report.fileReport.maxFileSize) {
               report.fileReport.maxFileSize = attrs.size();
            }
            if(report.fileReport.minFileSize == 0) {
               report.fileReport.minFileSize = attrs.size();
            }
            else if(attrs.size() < report.fileReport.minFileSize) {
               report.fileReport.minFileSize = attrs.size();
            }
            report.fileReport.size += attrs.size();               
            
            CMDInstance instance = ctx.getBean(CMDInstance.class, file, attrs.size());
            
            executor.submit(() -> {
               
               try {
                  CMDInstanceReport cmdInstanceReport = instance.generateReport(report.getName());
                  cmdInstanceReport.mergeWithParent(report);
               }
               catch (SubprocessorException e) {

                  log.debug("Error while generating report for instance: " + instance.getPath() + ":" + e.getMessage()
                        + " Skipping to next instance...");
                  new ErrorReport(conf.getDirectory().getDataRoot().relativize(instance.getPath()).toString(), e.getMessage())
                        .mergeWithParent(report);
               }
            }); // end executor.submit            
            
            return FileVisitResult.CONTINUE;
         }

         @Override
         public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            log.error("can't access file '{}'", file);
            return FileVisitResult.CONTINUE;
         }

         @Override
         public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {

            return FileVisitResult.CONTINUE;
         }      
      });

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
      
      try (Stream<AggregatedStatus> stream = aRep.findAllByProvidergroupName(report.getName())) {

         stream.forEach(categoryStats -> {
            Statistics xmlStatistics = new Statistics();
            xmlStatistics.avgRespTime = categoryStats.getAvgDuration();
            xmlStatistics.maxRespTime = categoryStats.getMaxDuration();
            xmlStatistics.category = categoryStats.getCategory();
            xmlStatistics.count = categoryStats.getNumber();
            report.urlReport.totNumOfCheckedLinks += categoryStats.getNumber();

            switch (categoryStats.getCategory()) {
               case Invalid_URL -> report.urlReport.totNumOfInvalidLinks = categoryStats.getNumber().intValue();
               case Broken -> report.urlReport.totNumOfBrokenLinks = categoryStats.getNumber().intValue();
               case Undetermined -> report.urlReport.totNumOfUndeterminedLinks = categoryStats.getNumber().intValue();
               case Restricted_Access ->
                  report.urlReport.totNumOfRestrictedAccessLinks = categoryStats.getNumber().intValue();
               case Blocked_By_Robots_txt ->
                  report.urlReport.totNumOfBlockedByRobotsTxtLinks = (int) categoryStats.getNumber().intValue();
               default -> throw new IllegalArgumentException("Unexpected value: " + categoryStats.getCategory());
            }

            report.urlReport.statistics.add(xmlStatistics);
         });
      }
      catch (Exception ex) {

         log.error("couldn't get category statistics for provider group '{}' from database", report.getName(), ex);
      }

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

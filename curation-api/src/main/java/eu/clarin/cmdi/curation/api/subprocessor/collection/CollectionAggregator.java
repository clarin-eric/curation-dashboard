package eu.clarin.cmdi.curation.api.subprocessor.collection;

import eu.clarin.linkchecker.persistence.model.AggregatedStatus;
import eu.clarin.linkchecker.persistence.repository.AggregatedStatusRepository;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.collection.sec.LinkcheckerReport.Statistics;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
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
public class CollectionAggregator extends AbstractSubprocessor<CMDCollection, CollectionReport>{

   @Autowired
   private ApiConfig conf;
   @Autowired
   private ApplicationContext ctx;
   @Autowired
   AggregatedStatusRepository aRep;

   @Transactional
   public void process(CMDCollection collection, CollectionReport collectionReport) {
      
      
      conf.getFacets().forEach(facetName -> collectionReport.getFacetReport().addFacet(facetName));


      ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(conf.getThreadPoolSize());    

      try {
         Files.walkFileTree(collection.getPath(), new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

               return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
               
               collectionReport.getFileReport().incrementNumOfFiles();
               
               if(attrs.size() > collectionReport.getFileReport().getMaxFileSize()) {
                  collectionReport.getFileReport().setMaxFileSize(attrs.size());
               }
               if(collectionReport.getFileReport().getMinFileSize() == 0 || attrs.size() < collectionReport.getFileReport().getMinFileSize()) {
                  collectionReport.getFileReport().setMinFileSize(attrs.size());
               }

               collectionReport.getFileReport().addSize(attrs.size());               
               
               CMDInstance instance = ctx.getBean(CMDInstance.class, filePath, attrs.size(), collectionReport.getFileReport().getProvider());
               
               executor.submit(() -> {
 
                  CMDInstanceReport cmdInstanceReport = instance.generateReport();
                  collectionReport.addReport(cmdInstanceReport);

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
      }
      catch (IOException e) {
         
         log.error("can't walk through path '{}'", collection.getPath());
         throw new RuntimeException(e);
      
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
   
      
      try (Stream<AggregatedStatus> stream = aRep.findAllByProvidergroupName(collectionReport.getName())) {

         stream.forEach(categoryStats -> {
            Statistics xmlStatistics = new Statistics();
            xmlStatistics.avgRespTime = categoryStats.getAvgDuration();
            xmlStatistics.maxRespTime = categoryStats.getMaxDuration();
            xmlStatistics.category = categoryStats.getCategory();
            xmlStatistics.count = categoryStats.getNumber();
            collectionReport.getLinkcheckerReport().setTotNumOfCheckedLinks(categoryStats.getNumber().intValue());

            switch (categoryStats.getCategory()) {
               case Invalid_URL -> collectionReport.getLinkcheckerReport().setTotNumOfInvalidLinks(categoryStats.getNumber().intValue());
               case Broken -> collectionReport.getLinkcheckerReport().setTotNumOfBrokenLinks(categoryStats.getNumber().intValue());
               case Undetermined -> collectionReport.getLinkcheckerReport().setTotNumOfUndeterminedLinks(categoryStats.getNumber().intValue());
               case Restricted_Access ->
               collectionReport.getLinkcheckerReport().setTotNumOfRestrictedAccessLinks(categoryStats.getNumber().intValue());
               case Blocked_By_Robots_txt ->
               collectionReport.getLinkcheckerReport().setTotNumOfBlockedByRobotsTxtLinks(categoryStats.getNumber().intValue());
               default -> throw new IllegalArgumentException("Unexpected value: " + categoryStats.getCategory());
            }

            collectionReport.getLinkcheckerReport().getStatistics().add(xmlStatistics);
         });
      }
      catch (IllegalArgumentException ex) {

         log.error(ex.getMessage());
         
      }
      catch (Exception ex) {

         log.error("couldn't get category statistics for provider group '{}' from database", collectionReport.getName(), ex);
      }
   }
}

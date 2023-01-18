package eu.clarin.cmdi.curation.api.subprocessor.collection;

import eu.clarin.linkchecker.persistence.model.AggregatedStatus;
import eu.clarin.linkchecker.persistence.repository.AggregatedStatusRepository;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.Scoring.Severity;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport.InvalidFile;
import eu.clarin.cmdi.curation.api.report.collection.sec.FacetReport.FacetCollectionStruct;
import eu.clarin.cmdi.curation.api.report.collection.sec.LinkcheckerReport.Statistics;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
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
public class CollectionAggregator {

   @Autowired
   private ApiConfig conf;
   @Autowired
   private ApplicationContext ctx;
   @Autowired
   AggregatedStatusRepository aRep;

   @Transactional
   public void process(CMDCollection collection, CollectionReport collectionReport) {
      
      
      conf.getFacets().forEach(facetName -> collectionReport.facetReport.facets.add(new FacetCollectionStruct(facetName)));


      ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(conf.getThreadPoolSize());    

      try {
         Files.walkFileTree(collection.getPath(), new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

               return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
               
               collectionReport.fileReport.numOfFiles++;
               
               if(attrs.size() > collectionReport.fileReport.maxFileSize) {
                  collectionReport.fileReport.maxFileSize = attrs.size();
               }
               if(collectionReport.fileReport.minFileSize == 0 || attrs.size() < collectionReport.fileReport.minFileSize) {
                  collectionReport.fileReport.minFileSize = attrs.size();
               }

               collectionReport.fileReport.size+=attrs.size();               
               
               CMDInstance instance = ctx.getBean(CMDInstance.class, filePath, attrs.size(), collectionReport.fileReport.provider);
               
               executor.submit(() -> {
 
                  CMDInstanceReport instanceReport = instance.generateReport();
                  addReport(collectionReport, instanceReport);

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
            collectionReport.linkcheckerReport.totNumOfCheckedLinks = categoryStats.getNumber().intValue();

            switch (categoryStats.getCategory()) {
               case Invalid_URL -> collectionReport.linkcheckerReport.totNumOfInvalidLinks = categoryStats.getNumber().intValue();
               case Broken -> collectionReport.linkcheckerReport.totNumOfBrokenLinks = categoryStats.getNumber().intValue();
               case Undetermined -> collectionReport.linkcheckerReport.totNumOfUndeterminedLinks = categoryStats.getNumber().intValue();
               case Restricted_Access ->
               collectionReport.linkcheckerReport.totNumOfRestrictedAccessLinks = categoryStats.getNumber().intValue();
               case Blocked_By_Robots_txt ->
               collectionReport.linkcheckerReport.totNumOfBlockedByRobotsTxtLinks = categoryStats.getNumber().intValue();
               default -> throw new IllegalArgumentException("Unexpected value: " + categoryStats.getCategory());
            }

            collectionReport.linkcheckerReport.statistics.add(xmlStatistics);
         });
      }
      catch (IllegalArgumentException ex) {

         log.error(ex.getMessage());
         
      }
      catch (Exception ex) {

         log.error("couldn't get category statistics for provider group '{}' from database", collectionReport.getName(), ex);
      }
      
      collectionReport.avgScore = (collectionReport.fileReport.numOfFiles>0?(double) collectionReport.score/collectionReport.fileReport.numOfFiles:0.0);
   }
   
   public synchronized void addReport(CollectionReport collectionReport, CMDInstanceReport instanceReport) {
      
      if(!instanceReport.isValid()) {
         
         collectionReport.invalidFiles.add(
               new InvalidFile(instanceReport.fileReport.location, 
               instanceReport.scoring.messages.stream().filter(message -> message.severity==Severity.FATAL).findFirst().get().issue
            ));

         return;
      }
      
      collectionReport.score += instanceReport.scoring.score;
      if (instanceReport.scoring.score > collectionReport.insMaxScore) {
          collectionReport.insMaxScore = instanceReport.scoring.score;
      }

      if (instanceReport.scoring.score < collectionReport.insMinScore)
          collectionReport.insMinScore = instanceReport.scoring.score;

      collectionReport.maxPossibleScoreInstance = instanceReport.scoring.maxScore;

      // ResProxies
      collectionReport.resProxyReport.totNumOfResProxies+=instanceReport.resProxyReport.numOfResProxies;
      collectionReport.resProxyReport.totNumOfResProxiesWithMime+=instanceReport.resProxyReport.numOfResourcesWithMime;
      collectionReport.resProxyReport.totNumOfResProxiesWithReference+=instanceReport.resProxyReport.numOfResProxiesWithReference;

      // XMLPopulatedValidator
      collectionReport.xmlPopulationReport.totNumOfXMLElements+=instanceReport.xmlPopulationReport.numOfXMLElements;
      collectionReport.xmlPopulationReport.totNumOfXMLSimpleElements+=instanceReport.xmlPopulationReport.numOfXMLSimpleElements;
      collectionReport.xmlPopulationReport.totNumOfXMLEmptyElements+=instanceReport.xmlPopulationReport.numOfXMLEmptyElements;




      // Facet
      instanceReport
         .facetReport
         .coverages
         .stream()
         .filter(coverage -> coverage.coveredByInstance)
         .forEach(instanceFacet -> collectionReport.facetReport.facets.stream().filter(collectionFacet -> collectionFacet.name.equals(instanceFacet.name)).findFirst().get().count++);


//      collectionReport.handleProfile(instanceReport.header.getId(), instanceReport.profileScore);
      
   }

}

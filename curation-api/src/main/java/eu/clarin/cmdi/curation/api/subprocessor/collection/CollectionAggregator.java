package eu.clarin.cmdi.curation.api.subprocessor.collection;

import eu.clarin.linkchecker.persistence.model.AggregatedStatus;
import eu.clarin.linkchecker.persistence.repository.AggregatedStatusRepository;
import eu.clarin.linkchecker.persistence.repository.UrlRepository;
import eu.clarin.linkchecker.persistence.utils.Category;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport.OriginIssue;
import eu.clarin.cmdi.curation.api.report.collection.sec.FacetReport.FacetCollectionStruct;
import eu.clarin.cmdi.curation.api.report.collection.sec.HeaderReport.Profile;
import eu.clarin.cmdi.curation.api.report.collection.sec.LinkcheckerReport.Statistics;
import eu.clarin.cmdi.curation.api.report.collection.sec.ResProxyReport.InvalidReference;
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
import java.util.ArrayList;
import java.util.Collection;
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
   private AggregatedStatusRepository aRep;
   @Autowired
   private UrlRepository uRep;
   
   private Collection<String> mdSelfLinks = new ArrayList<String>();

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
      
      calculateAverages(collectionReport);

   }
   
   public void addReport(CollectionReport collectionReport, CMDInstanceReport instanceReport) {
      
     if(instanceReport.issues.size() > 0) {
        collectionReport.issues.add(new OriginIssue(instanceReport.fileReport.location, instanceReport.issues));
     }
      
      if(instanceReport.isValidReport) {
         

         
         if (instanceReport.instanceScore > collectionReport.insMaxScore) {
             collectionReport.insMaxScore = instanceReport.instanceScore;
         }
   
         if (instanceReport.instanceScore < collectionReport.insMinScore)
             collectionReport.insMinScore = instanceReport.instanceScore;
   
         //file
         collectionReport.fileReport.numOfValidFiles++;
         collectionReport.aggregatedScore+=instanceReport.fileReport.score;
         
         //header
         synchronized(this) {
            collectionReport.headerReport.profiles.stream()
               .filter(profile -> profile.profileId.equals(instanceReport.profileHeaderReport.getId()))
               .findFirst()
               .ifPresentOrElse(
                     profile -> profile.count++, 
                     () -> collectionReport.headerReport.profiles.add(new Profile(instanceReport.profileHeaderReport.getId(), instanceReport.profileScore))
                  );
         }
         
         if(instanceReport.instanceHeaderReport.mdSelfLink != null && !instanceReport.instanceHeaderReport.mdSelfLink.isEmpty()) {
            synchronized(this) {
               if(this.mdSelfLinks.contains(instanceReport.instanceHeaderReport.mdSelfLink)) {
                  collectionReport.headerReport.duplicatedMDSelfLink.add(instanceReport.instanceHeaderReport.mdSelfLink);
               }
               else {
                  this.mdSelfLinks.add(instanceReport.instanceHeaderReport.mdSelfLink);
               }
            }
         }
         
         collectionReport.headerReport.aggregatedScore+= (instanceReport.profileHeaderReport.score + instanceReport.profileScore);
         
   
         // ResProxies
         collectionReport.resProxyReport.totNumOfResProxies+=instanceReport.resProxyReport.numOfResProxies;
         collectionReport.resProxyReport.totNumOfResProxiesWithMime+=instanceReport.resProxyReport.numOfResourcesWithMime;
         collectionReport.resProxyReport.totNumOfResProxiesWithReference+=instanceReport.resProxyReport.numOfResProxiesWithReference;
         
         if(instanceReport.resProxyReport.invalidReferences.size() > 0) {
            collectionReport.resProxyReport.invalidReferences.add(new InvalidReference(instanceReport.fileReport.location, instanceReport.resProxyReport.invalidReferences));
         } 
         collectionReport.resProxyReport.aggregatedScore = instanceReport.resProxyReport.score;
         
   
         // XmlPopulation
         collectionReport.xmlPopulationReport.totNumOfXMLElements+=instanceReport.xmlPopulationReport.numOfXMLElements;
         collectionReport.xmlPopulationReport.totNumOfXMLSimpleElements+=instanceReport.xmlPopulationReport.numOfXMLSimpleElements;
         collectionReport.xmlPopulationReport.totNumOfXMLEmptyElements+=instanceReport.xmlPopulationReport.numOfXMLEmptyElements;
         collectionReport.xmlPopulationReport.aggregatedScore+=instanceReport.xmlPopulationReport.score;
         
         // XmlValidation
         collectionReport.xmlValidationReport.totNumOfValidRecords+=instanceReport.xmlValidityReport.score;
         collectionReport.xmlValidationReport.aggregatedScore+=instanceReport.xmlValidityReport.score;
   
   
         // Facet
         synchronized(this) {
            instanceReport
               .facetReport
               .coverages
               .stream()
               .filter(coverage -> coverage.coveredByInstance)
               .forEach(instanceFacet -> collectionReport.facetReport.facets.stream().filter(collectionFacet -> collectionFacet.name.equals(instanceFacet.name)).findFirst().get().count++);
         }
      }
      collectionReport.facetReport.aggregatedScore+=instanceReport.facetReport.score;
      
      collectionReport.aggregatedScore += instanceReport.instanceScore;
   }
   
   private void calculateAverages(CollectionReport collectionReport) {
      //lincheckerReport
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
      
      collectionReport.linkcheckerReport.totNumOfLinks = (int) uRep.countByProvidergroupName(collectionReport.getName());
      collectionReport.linkcheckerReport.totNumOfUniqueLinks = (int) uRep.countDistinctByProvidergroupName(collectionReport.getName());
      if(collectionReport.linkcheckerReport.totNumOfLinks>0) {
         collectionReport.linkcheckerReport.statistics.stream()
               .filter(statistics -> statistics.category == Category.Ok)
               .findFirst()
               .ifPresent(statistics -> collectionReport.linkcheckerReport.ratioOfValidLinks = (collectionReport.linkcheckerReport.totNumOfLinks>0?(double) (statistics.count/collectionReport.linkcheckerReport.totNumOfLinks):1.0));
      }
      
      if(collectionReport.fileReport.numOfFiles >0) {
         //file
         collectionReport.fileReport.avgFileSize = collectionReport.fileReport.size/collectionReport.fileReport.numOfFiles;
         collectionReport.fileReport.avgScore = (double) (collectionReport.fileReport.aggregatedScore/collectionReport.fileReport.numOfFiles);
         //header
         collectionReport.headerReport.totNumOfProfiles = collectionReport.headerReport.profiles.size();
         collectionReport.headerReport.avgScore = (collectionReport.headerReport.aggregatedScore/collectionReport.fileReport.numOfFiles);
         //resProxy
         collectionReport.resProxyReport.avgNumOfResProxies = (double)  (collectionReport.resProxyReport.totNumOfResProxies/collectionReport.fileReport.numOfFiles);
         collectionReport.resProxyReport.avgNumOfResProxiesWithMime = (double) (collectionReport.resProxyReport.totNumOfResProxiesWithMime/collectionReport.fileReport.numOfFiles);
         collectionReport.resProxyReport.avgNumOfResProxiesWithReference = (double)  (collectionReport.resProxyReport.totNumOfResProxiesWithReference/collectionReport.fileReport.numOfFiles);
         collectionReport.resProxyReport.avgScore = (double) (collectionReport.resProxyReport.aggregatedScore/collectionReport.fileReport.numOfFiles);
         //XmlPopulation
         collectionReport.xmlPopulationReport.avgNumOfXMLElements = collectionReport.xmlPopulationReport.totNumOfXMLElements/collectionReport.fileReport.numOfFiles;
         collectionReport.xmlPopulationReport.avgNumOfXMLEmptyElements = collectionReport.xmlPopulationReport.totNumOfXMLEmptyElements/collectionReport.fileReport.numOfFiles;
         collectionReport.xmlPopulationReport.avgScore = (double) (collectionReport.xmlPopulationReport.aggregatedScore/collectionReport.fileReport.numOfFiles);
         if(collectionReport.xmlPopulationReport.totNumOfXMLSimpleElements>0) {
            collectionReport.xmlPopulationReport.avgRateOfPopulatedElement = (1.0 - collectionReport.xmlPopulationReport.totNumOfXMLEmptyElements/collectionReport.xmlPopulationReport.totNumOfXMLSimpleElements);
         }
         //XmlValidation
         collectionReport.xmlValidationReport.avgScore = (double) (collectionReport.xmlValidationReport.aggregatedScore/collectionReport.fileReport.numOfFiles);
         
         //Facet
         collectionReport.facetReport.facets
            .forEach(facet -> facet.coverage = facet.count/collectionReport.fileReport.numOfFiles);
         
         collectionReport.facetReport.avgScore = (double) (collectionReport.facetReport.aggregatedScore/collectionReport.fileReport.numOfFiles);
         collectionReport.facetReport.percCoverageNonZero = (double) collectionReport.facetReport.facets.stream().filter(facet -> facet.count > 0).count()/collectionReport.facetReport.facets.size();
         
         collectionReport.avgScore = collectionReport.aggregatedScore/collectionReport.fileReport.numOfFiles;
         //linkchecker
         collectionReport.linkcheckerReport.avgNumOfLinks = (double) collectionReport.linkcheckerReport.totNumOfLinks/collectionReport.fileReport.numOfFiles;
         collectionReport.linkcheckerReport.avgNumOfUniqueLinks = (double) collectionReport.linkcheckerReport.totNumOfUniqueLinks/collectionReport.fileReport.numOfFiles;
         collectionReport.linkcheckerReport.avgNumOfBrokenLinks = (double) collectionReport.linkcheckerReport.avgNumOfBrokenLinks/collectionReport.fileReport.numOfFiles;
         collectionReport.linkcheckerReport.maxRespTime = collectionReport.linkcheckerReport.statistics.stream().mapToLong(statistics -> statistics.maxRespTime).max().orElse(0);
         collectionReport.linkcheckerReport.avgRespTime = collectionReport.linkcheckerReport.statistics.stream().mapToDouble(statistics -> statistics.avgRespTime*statistics.nonNullCount).average().orElse(0.0);
      }
      
      if(collectionReport.fileReport.numOfValidFiles >0) {
         //file
         collectionReport.fileReport.avgScoreValid = (double) (collectionReport.fileReport.aggregatedScore/collectionReport.fileReport.numOfValidFiles);
         //header
         collectionReport.headerReport.avgScoreValid = (collectionReport.headerReport.aggregatedScore/collectionReport.fileReport.numOfValidFiles);
         //resProxy
         collectionReport.resProxyReport.avgScoreValid = (double) (collectionReport.resProxyReport.aggregatedScore/collectionReport.fileReport.numOfValidFiles);
         //xmlpopulation
         collectionReport.xmlPopulationReport.avgScoreValid = (double) (collectionReport.xmlPopulationReport.aggregatedScore/collectionReport.fileReport.numOfValidFiles);
         //xmlvalidation
         collectionReport.xmlValidationReport.avgScoreValid = (double) (collectionReport.xmlValidationReport.aggregatedScore/collectionReport.fileReport.numOfValidFiles);
         //facet
         collectionReport.facetReport.avgScoreValid = (double) (collectionReport.facetReport.aggregatedScore/collectionReport.fileReport.numOfValidFiles);
         
         collectionReport.avgScoreValid = collectionReport.aggregatedScore/collectionReport.fileReport.numOfValidFiles;
         
      }
      
      collectionReport.avgScore = (collectionReport.fileReport.numOfFiles>0?(double) collectionReport.aggregatedScore/collectionReport.fileReport.numOfFiles:0.0);      
   }
}

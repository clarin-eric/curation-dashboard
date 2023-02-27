package eu.clarin.cmdi.curation.api.subprocessor.collection;

import eu.clarin.linkchecker.persistence.model.AggregatedStatus;
import eu.clarin.linkchecker.persistence.repository.AggregatedStatusRepository;
import eu.clarin.linkchecker.persistence.repository.UrlRepository;
import eu.clarin.linkchecker.persistence.utils.Category;
import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CMDCollection;
import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport.RecordDetail;
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

      conf.getFacets()
            .forEach(facetName -> collectionReport.facetReport.facets.add(new FacetCollectionStruct(facetName)));

      ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(conf.getThreadpoolSize());

      try {
         Files.walkFileTree(collection.getPath(), new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

               return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {

               collectionReport.fileReport.numOfFiles++;

               if (attrs.size() > collectionReport.fileReport.maxFileSize) {
                  collectionReport.fileReport.maxFileSize = attrs.size();
               }
               if (collectionReport.fileReport.minFileSize == 0
                     || attrs.size() < collectionReport.fileReport.minFileSize) {
                  collectionReport.fileReport.minFileSize = attrs.size();
               }

               collectionReport.fileReport.size += attrs.size();

               CMDInstance instance = ctx.getBean(CMDInstance.class, filePath, attrs.size(),
                     collectionReport.fileReport.provider);

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

   public synchronized void addReport(CollectionReport collectionReport, CMDInstanceReport instanceReport) {
      
      if(instanceReport.details.size() > 0) {//only add a record if there're details to report
         
         collectionReport.recordDetails.add(new RecordDetail(instanceReport.fileReport.location, instanceReport.details));
      }

      if (instanceReport.isProcessable) {

         if (instanceReport.instanceScore > collectionReport.maxScore) {
            collectionReport.maxScore = instanceReport.instanceScore;
         }

         if (instanceReport.instanceScore < collectionReport.minScore)
            collectionReport.minScore = instanceReport.instanceScore;

         // file
         collectionReport.fileReport.numOfFilesProcessable++;
         collectionReport.fileReport.aggregatedScore += instanceReport.fileReport.score;

         // header

         collectionReport.headerReport.profiles.stream()
               .filter(profile -> profile.profileId.equals(instanceReport.profileHeaderReport.getId())).findFirst()
               .ifPresentOrElse(profile -> profile.count++, () -> collectionReport.headerReport.profiles
                     .add(new Profile(instanceReport.profileHeaderReport.getId(), instanceReport.profileScore)));


         if (instanceReport.instanceHeaderReport.mdSelfLink != null
               && !instanceReport.instanceHeaderReport.mdSelfLink.isEmpty()) {

            if (this.mdSelfLinks.contains(instanceReport.instanceHeaderReport.mdSelfLink)) {
               collectionReport.headerReport.duplicatedMDSelfLink
                     .add(instanceReport.instanceHeaderReport.mdSelfLink);
            }
            else {
               this.mdSelfLinks.add(instanceReport.instanceHeaderReport.mdSelfLink);
            }
         }

         collectionReport.headerReport.aggregatedScore += instanceReport.instanceHeaderReport.score;
         collectionReport.headerReport.aggregatedScore += instanceReport.profileHeaderReport.score;

         // ResProxies
         collectionReport.resProxyReport.totNumOfResProxies += instanceReport.resProxyReport.numOfResProxies;
         collectionReport.resProxyReport.totNumOfResProxiesWithMime += instanceReport.resProxyReport.numOfResourcesWithMime;
         collectionReport.resProxyReport.totNumOfResProxiesWithReference += instanceReport.resProxyReport.numOfResProxiesWithReference;

         if (instanceReport.resProxyReport.invalidReferences.size() > 0) {
            collectionReport.resProxyReport.invalidReferences.add(new InvalidReference(
                  instanceReport.fileReport.location, instanceReport.resProxyReport.invalidReferences));
         }
         collectionReport.resProxyReport.aggregatedScore += instanceReport.resProxyReport.score;

         // XmlPopulation
         collectionReport.xmlPopulationReport.totNumOfXMLElements += instanceReport.xmlPopulationReport.numOfXMLElements;
         collectionReport.xmlPopulationReport.totNumOfXMLSimpleElements += instanceReport.xmlPopulationReport.numOfXMLSimpleElements;
         collectionReport.xmlPopulationReport.totNumOfXMLEmptyElements += instanceReport.xmlPopulationReport.numOfXMLEmptyElements;
         collectionReport.xmlPopulationReport.aggregatedScore += instanceReport.xmlPopulationReport.score;

         // XmlValidation
         collectionReport.xmlValidityReport.totNumOfValidRecords += instanceReport.xmlValidityReport.score;
         collectionReport.xmlValidityReport.aggregatedScore += instanceReport.xmlValidityReport.score;

         // Facet
         synchronized (this) {
            instanceReport.facetReport.coverages.stream().filter(coverage -> coverage.coveredByInstance)
                  .forEach(instanceFacet -> collectionReport.facetReport.facets.stream()
                        .filter(collectionFacet -> collectionFacet.name.equals(instanceFacet.name)).findFirst()
                        .get().count++);
         }
      }
      collectionReport.facetReport.aggregatedScore += instanceReport.facetReport.score;

      collectionReport.aggregatedScore += instanceReport.instanceScore;
   }

   private void calculateAverages(CollectionReport collectionReport) {
      // lincheckerReport

      try (Stream<AggregatedStatus> stream = aRep.findAllByProvidergroupName(collectionReport.getName())) {

         stream.forEach(categoryStats -> {
            Statistics xmlStatistics = new Statistics(categoryStats.getCategory());
            xmlStatistics.avgRespTime = categoryStats.getAvgDuration();
            xmlStatistics.maxRespTime = categoryStats.getMaxDuration();
            xmlStatistics.count = categoryStats.getNumber();
            collectionReport.linkcheckerReport.totNumOfCheckedLinks += categoryStats.getNumber().intValue();

            collectionReport.linkcheckerReport.statistics.add(xmlStatistics);
         });
      }
      catch (IllegalArgumentException ex) {

         log.error(ex.getMessage());

      }
      catch (Exception ex) {

         log.error("couldn't get category statistics for provider group '{}' from database", collectionReport.getName(),
               ex);
      }

      collectionReport.linkcheckerReport.totNumOfLinks = (int) uRep
            .countByProvidergroupName(collectionReport.getName());
      collectionReport.linkcheckerReport.totNumOfUniqueLinks = (int) uRep
            .countDistinctByProvidergroupName(collectionReport.getName());

      if (collectionReport.linkcheckerReport.totNumOfLinks > 0) {
         collectionReport.linkcheckerReport.statistics.stream().filter(statistics -> statistics.category == Category.Ok)
               .findFirst().ifPresent(
                     statistics -> collectionReport.linkcheckerReport.ratioOfValidLinks = (collectionReport.linkcheckerReport.totNumOfLinks > 0
                           ? (statistics.count /(double) collectionReport.linkcheckerReport.totNumOfLinks)
                           : 0.0));
      }

      collectionReport.linkcheckerReport.aggregatedScore = (double) collectionReport.linkcheckerReport.ratioOfValidLinks
            * collectionReport.fileReport.numOfFilesProcessable;
      collectionReport.linkcheckerReport.aggregatedMaxScoreAll = (double) collectionReport.fileReport.numOfFiles;
      collectionReport.linkcheckerReport.aggregatedMaxScoreProcessable = collectionReport.fileReport.numOfFilesProcessable;
      collectionReport.linkcheckerReport.avgScoreProcessable = collectionReport.linkcheckerReport.ratioOfValidLinks;

      collectionReport.fileReport.aggregatedMaxScoreAll = eu.clarin.cmdi.curation.api.report.instance.sec.FileReport.maxScore
            * collectionReport.fileReport.numOfFiles;
      collectionReport.fileReport.aggregatedMaxScoreProcessable = eu.clarin.cmdi.curation.api.report.instance.sec.FileReport.maxScore
            * collectionReport.fileReport.numOfFilesProcessable;

      collectionReport.headerReport.aggregatedMaxScoreAll = eu.clarin.cmdi.curation.api.report.instance.sec.InstanceHeaderReport.maxScore
            * collectionReport.fileReport.numOfFiles;
      collectionReport.headerReport.aggregatedMaxScoreProcessable = eu.clarin.cmdi.curation.api.report.instance.sec.InstanceHeaderReport.maxScore
            * collectionReport.fileReport.numOfFilesProcessable;

      collectionReport.resProxyReport.aggregatedMaxScoreAll = eu.clarin.cmdi.curation.api.report.instance.sec.ResourceProxyReport.maxScore
            * collectionReport.fileReport.numOfFiles;
      collectionReport.resProxyReport.aggregatedMaxScoreProcessable = eu.clarin.cmdi.curation.api.report.instance.sec.ResourceProxyReport.maxScore
            * collectionReport.fileReport.numOfFilesProcessable;

      collectionReport.xmlPopulationReport.aggregatedMaxScoreAll = eu.clarin.cmdi.curation.api.report.instance.sec.XmlPopulationReport.maxScore
            * collectionReport.fileReport.numOfFiles;
      collectionReport.xmlPopulationReport.aggregatedMaxScoreProcessable = eu.clarin.cmdi.curation.api.report.instance.sec.XmlPopulationReport.maxScore
            * collectionReport.fileReport.numOfFilesProcessable;

      collectionReport.xmlValidityReport.aggregatedMaxScoreAll = eu.clarin.cmdi.curation.api.report.instance.sec.XmlValidityReport.maxScore
            * collectionReport.fileReport.numOfFiles;
      collectionReport.xmlValidityReport.aggregatedMaxScoreProcessable = eu.clarin.cmdi.curation.api.report.instance.sec.XmlValidityReport.maxScore
            * collectionReport.fileReport.numOfFilesProcessable;
      
      collectionReport.facetReport.aggregatedMaxScoreAll = eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport.maxScore
            * collectionReport.fileReport.numOfFiles;     
      collectionReport.facetReport.aggregatedMaxScoreProcessable = eu.clarin.cmdi.curation.api.report.instance.sec.InstanceFacetReport.maxScore
            * collectionReport.fileReport.numOfFilesProcessable;      

      collectionReport.aggregatedMaxScoreAll = (eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport.maxScore + 1)
            * collectionReport.fileReport.numOfFiles;
      collectionReport.aggregatedMaxScoreProcessable = (eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport.maxScore
            + 1) * collectionReport.fileReport.numOfFilesProcessable;

      if (collectionReport.fileReport.numOfFiles > 0) {
         // file
         collectionReport.fileReport.avgFileSize = collectionReport.fileReport.size
               / collectionReport.fileReport.numOfFiles;
         collectionReport.fileReport.avgScoreAll = (double) (collectionReport.fileReport.aggregatedScore
               / collectionReport.fileReport.numOfFiles);
         collectionReport.fileReport.scorePercentageAll = collectionReport.fileReport.aggregatedScore
               / (double) collectionReport.fileReport.aggregatedMaxScoreAll;
         
         // header
         collectionReport.headerReport.totNumOfProfiles = collectionReport.headerReport.profiles.size();
         collectionReport.headerReport.avgScoreAll = (collectionReport.headerReport.aggregatedScore
               / collectionReport.fileReport.numOfFiles);
         collectionReport.headerReport.scorePercentageAll = collectionReport.headerReport.aggregatedScore
               / (double) collectionReport.headerReport.aggregatedMaxScoreAll;
         // resProxy
         collectionReport.resProxyReport.avgNumOfResProxies = (collectionReport.resProxyReport.totNumOfResProxies
               / (double) collectionReport.fileReport.numOfFiles);
         collectionReport.resProxyReport.avgNumOfResProxiesWithMime = (collectionReport.resProxyReport.totNumOfResProxiesWithMime
               / (double) collectionReport.fileReport.numOfFiles);
         collectionReport.resProxyReport.avgNumOfResProxiesWithReference = (collectionReport.resProxyReport.totNumOfResProxiesWithReference
               / (double) collectionReport.fileReport.numOfFiles);
         collectionReport.resProxyReport.avgScoreAll = collectionReport.resProxyReport.aggregatedScore
               / (double) collectionReport.fileReport.numOfFiles;
         collectionReport.resProxyReport.scorePercentageAll = collectionReport.resProxyReport.aggregatedScore
               / (double) collectionReport.resProxyReport.aggregatedMaxScoreAll;
         // XmlPopulation
         collectionReport.xmlPopulationReport.avgNumOfXMLElements = collectionReport.xmlPopulationReport.totNumOfXMLElements
               / (double) collectionReport.fileReport.numOfFiles;
         collectionReport.xmlPopulationReport.avgNumOfXMLSimpleElements = collectionReport.xmlPopulationReport.totNumOfXMLSimpleElements
               / (double) collectionReport.fileReport.numOfFiles;
         collectionReport.xmlPopulationReport.avgNumOfXMLEmptyElements = collectionReport.xmlPopulationReport.totNumOfXMLEmptyElements
               / collectionReport.fileReport.numOfFiles;
         collectionReport.xmlPopulationReport.avgScoreAll = (double) (collectionReport.xmlPopulationReport.aggregatedScore
               / collectionReport.fileReport.numOfFiles);
         if (collectionReport.xmlPopulationReport.totNumOfXMLSimpleElements > 0) {
            collectionReport.xmlPopulationReport.avgRateOfPopulatedElements = (1.0
                  - collectionReport.xmlPopulationReport.totNumOfXMLEmptyElements
                        / (double) collectionReport.xmlPopulationReport.totNumOfXMLSimpleElements);
         }
         collectionReport.xmlPopulationReport.scorePercentageAll = collectionReport.xmlPopulationReport.aggregatedScore
               / (double) collectionReport.xmlPopulationReport.aggregatedMaxScoreAll;
         // XmlValidation
         collectionReport.xmlValidityReport.avgScoreAll = (collectionReport.xmlValidityReport.aggregatedScore
               / (double) collectionReport.fileReport.numOfFiles);

         // Facet
         collectionReport.facetReport.facets
               .forEach(facet -> facet.avgCoverage = facet.count / (double) collectionReport.fileReport.numOfFiles);

         collectionReport.facetReport.avgScoreAll = (double) (collectionReport.facetReport.aggregatedScore
               / collectionReport.fileReport.numOfFiles);
         collectionReport.facetReport.percCoverageNonZero = (double) collectionReport.facetReport.facets.stream()
               .filter(facet -> facet.count > 0).count() / collectionReport.facetReport.facets.size();

         // linkchecker
         collectionReport.linkcheckerReport.avgNumOfLinks = collectionReport.linkcheckerReport.totNumOfLinks
               / (double) collectionReport.fileReport.numOfFiles;
         collectionReport.linkcheckerReport.avgNumOfUniqueLinks = collectionReport.linkcheckerReport.totNumOfUniqueLinks
               / (double) collectionReport.fileReport.numOfFiles;
         collectionReport.linkcheckerReport.maxRespTime = collectionReport.linkcheckerReport.statistics.stream()
               .filter(statistics -> statistics.maxRespTime != null).mapToLong(statistics -> statistics.maxRespTime)
               .max().orElse(0);
         collectionReport.linkcheckerReport.avgRespTime = collectionReport.linkcheckerReport.statistics.stream()
               .filter(statistics -> statistics.avgRespTime != null)
               .mapToDouble(statistics -> statistics.avgRespTime * statistics.count).average().orElse(0.0);
         collectionReport.linkcheckerReport.scorePercentageAll = collectionReport.linkcheckerReport.aggregatedScore
               / (double) collectionReport.linkcheckerReport.aggregatedMaxScoreAll;
         //collection
         collectionReport.avgScoreAll = collectionReport.aggregatedScore / collectionReport.fileReport.numOfFiles;
         collectionReport.scorePercentageAll = collectionReport.aggregatedScore / collectionReport.aggregatedMaxScoreAll;

      }

      if (collectionReport.fileReport.numOfFilesProcessable > 0) {
         // file
         collectionReport.fileReport.scorePercentageProcessable = collectionReport.fileReport.aggregatedScore 
               / (double) collectionReport.fileReport.aggregatedMaxScoreProcessable;
         collectionReport.fileReport.avgScoreProcessable = (collectionReport.fileReport.aggregatedScore
               / (double) collectionReport.fileReport.numOfFilesProcessable);
         // header
         collectionReport.headerReport.scorePercentageProcessable = collectionReport.headerReport.aggregatedScore
               / (double) collectionReport.headerReport.aggregatedMaxScoreProcessable;
         collectionReport.headerReport.avgScoreProcessable = (collectionReport.headerReport.aggregatedScore
               / (double) collectionReport.fileReport.numOfFilesProcessable);
         // resProxy
         collectionReport.resProxyReport.scorePercentageProcessable = collectionReport.resProxyReport.aggregatedScore
               / collectionReport.resProxyReport.aggregatedMaxScoreProcessable;
         collectionReport.resProxyReport.avgScoreProcessable = (collectionReport.resProxyReport.aggregatedScore
               / (double) collectionReport.fileReport.numOfFilesProcessable);
         // xmlpopulation
         collectionReport.xmlPopulationReport.scorePercentageProcessable = collectionReport.xmlPopulationReport.aggregatedScore
               / (double) collectionReport.xmlPopulationReport.aggregatedMaxScoreProcessable;
         collectionReport.xmlPopulationReport.avgScoreProcessable = (collectionReport.xmlPopulationReport.aggregatedScore
               / (double) collectionReport.fileReport.numOfFilesProcessable);
         // xmlvalidity
         collectionReport.xmlValidityReport.scorePercentageProcessable = collectionReport.xmlValidityReport.aggregatedScore
               / (double) collectionReport.xmlValidityReport.aggregatedMaxScoreProcessable;
         collectionReport.xmlValidityReport.avgScoreProcessable = (collectionReport.xmlValidityReport.aggregatedScore
               / (double) collectionReport.fileReport.numOfFilesProcessable);
         collectionReport.xmlValidityReport.scorePercentageAll = collectionReport.xmlValidityReport.aggregatedScore
               / (double) collectionReport.xmlValidityReport.aggregatedMaxScoreAll;
         // facet
         collectionReport.facetReport.scorePercentageProcessable = collectionReport.facetReport.aggregatedScore
               / (double) collectionReport.facetReport.aggregatedMaxScoreProcessable;
         collectionReport.facetReport.avgScoreProcessable = (collectionReport.facetReport.aggregatedScore
               / (double) collectionReport.fileReport.numOfFilesProcessable);
         collectionReport.facetReport.scorePercentageAll = collectionReport.facetReport.aggregatedScore
               / (double) collectionReport.facetReport.aggregatedMaxScoreAll; 
         //linkchecker
         collectionReport.linkcheckerReport.scorePercentageProcessable = collectionReport.linkcheckerReport.aggregatedScore
               / (double) collectionReport.linkcheckerReport.aggregatedMaxScoreProcessable;
         //collection
         collectionReport.avgScoreProcessable = collectionReport.aggregatedScore
               / (double) collectionReport.fileReport.numOfFilesProcessable;         
         collectionReport.scorePercentageProcessable = collectionReport.aggregatedScore 
               / (double) collectionReport.aggregatedMaxScoreProcessable;

      }      




   }
}

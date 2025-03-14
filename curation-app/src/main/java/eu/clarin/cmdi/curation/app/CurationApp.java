package eu.clarin.cmdi.curation.app;

import eu.clarin.cmdi.curation.api.CurationModule;
import eu.clarin.cmdi.curation.api.entity.CurationEntityType;
import eu.clarin.cmdi.curation.api.report.collection.AllCollectionReport;
import eu.clarin.cmdi.curation.api.report.collection.CollectionHistoryReport;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.linkchecker.AllLinkcheckerReport;
import eu.clarin.cmdi.curation.api.report.profile.AllProfileReport;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.utils.FileStorage;
import eu.clarin.cmdi.curation.app.conf.AppConfig;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.linkchecker.persistence.service.LinkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * The type Curation app.
 */
@SpringBootApplication
@ComponentScan({"eu.clarin.cmdi.curation", "eu.clarin.linkchecker.persistence"})
@EnableJpaRepositories(basePackages = "eu.clarin.linkchecker.persistence.repository")
@EntityScan(basePackages = "eu.clarin.linkchecker.persistence.model")
@EnableCaching
@EnableConfigurationProperties
@Slf4j
public class CurationApp {

   private static final Pattern pattern = Pattern.compile("(\\S+)_(\\d{4}-\\d{2}-\\d{2}).html");
   
   @Autowired
   private AppConfig conf;   
   @Autowired
   private CurationModule curation;
   @Autowired
   private CRService crService;
   /**
    * The Storage.
    */
   @Autowired
   FileStorage storage;
   /**
    * The Link service.
    */
   @Autowired
   LinkService linkService;
   @Autowired
   CacheManager cacheManager;

   /**
    * The entry point of application.
    *
    * @param args the input arguments
    */
   public static void main(String[] args) {
		SpringApplication.run(CurationApp.class, args);
	}

   /**
    * Command line runner command line runner.
    *
    * @param ctx the ctx
    * @return the command line runner
    */
   @Bean
   public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
      
      return args -> {   
         
         log.info("start time: {}", LocalDateTime.now());
         
         if("all".equalsIgnoreCase(conf.getMode()) || "collection".equalsIgnoreCase(conf.getMode())) {
         
        
            final AllCollectionReport allCollectionReport = new AllCollectionReport();
            final AllLinkcheckerReport allLinkcheckerReport = new AllLinkcheckerReport();
            final CollectionHistoryReport collectionHistoryReport = new CollectionHistoryReport();

            conf.getDirectory().getIn().forEach(inPath -> {
                try {
                    processCollection(inPath, allCollectionReport,allLinkcheckerReport );
                }
                catch (MalFunctioningProcessorException e) {
                    throw new RuntimeException(e);
                }
            });

            // remove old reports
            if(conf.getPurgeReportAfter() > 0) {
               log.info("purging reports older than {} days from path '{}'", conf.getPurgeReportAfter(), conf.getDirectory().getOut());
               this.storage.purgeReportAfter(conf.getPurgeReportAfter());
               log.info("done purging reports");
            }

            // create a meta report of all collection reports
            Files.walk(conf.getDirectory().getOut().resolve("html").resolve("collection")).forEach(path -> {

               Matcher matcher;

               if(!path.getFileName().toString().startsWith("AllCollectionReport") && (matcher = pattern.matcher(path.getFileName().toString())).matches()) {

                  collectionHistoryReport.addReport(matcher.group(1).trim(), matcher.group(2), path.getFileName().toString());
               }
            });

            // save reports
            storage.saveReport(allCollectionReport, CurationEntityType.COLLECTION, true);
            storage.saveReport(allLinkcheckerReport, CurationEntityType.LINKCHECKER, true);
            storage.saveReport(collectionHistoryReport, CurationEntityType.COLLECTION, false);


         }
         // it's important to process profiles after collections, to fill the collection usage section of the profiles 
         // before they're printed out
         if("all".equalsIgnoreCase(conf.getMode()) || "profile".equalsIgnoreCase(conf.getMode())) {
            
            final AllProfileReport allProfileReport = new AllProfileReport();

            final Cache cache = this.cacheManager.getCache("profileReportCache");

            crService.getPublicSchemaLocations().forEach(schemaLocation -> { //this is for the public profiles

               log.info("start processing public profile '{}'", schemaLocation);

               CMDProfileReport profileReport = curation.processCMDProfile(schemaLocation);

               allProfileReport.addReport(profileReport);

               storage.saveReport(profileReport, CurationEntityType.PROFILE, false);

               cache.evict(schemaLocation);

               log.info("done processing public profile '{}'", schemaLocation);
            });

            // now we have to do the same for the non public profiles used by CMDI files

            Iterator<javax.cache.Cache.Entry<String,CMDProfileReport>> it = ((javax.cache.Cache) cache.getNativeCache()).iterator();

            it.forEachRemaining(entry -> {

               log.info("start processing non public profile '{}'", entry.getValue().headerReport.getSchemaLocation());
               allProfileReport.addReport(entry.getValue());

               storage.saveReport(entry.getValue(), CurationEntityType.PROFILE, false);
               log.info("done processing non public profile '{}'", entry.getValue().headerReport.getSchemaLocation());
            });

            storage.saveReport(allProfileReport, CurationEntityType.PROFILE, false);
         }
         
         if("all".equalsIgnoreCase(conf.getMode()) || "linkchecker".equalsIgnoreCase(conf.getMode())) {
            
            log.info("start writing linkchecker detail reports");
            curation.getLinkcheckerDetailReports().forEach(report -> storage.saveReport(report, CurationEntityType.LINKCHECKER, false));
            log.info("done writing linkchecker detail reports");
         }

         if("all".equalsIgnoreCase(conf.getMode())) {
            if(conf.getLinkDeactivationAfter() > 0) {
               log.info("start deactivating links older than {} days", conf.getLinkDeactivationAfter());
               linkService.deactivateLinksOlderThan(conf.getLinkDeactivationAfter());
               log.info("done deactivating links");
            }
            if(conf.getLinkDeletionAfter() > 0) {
               log.info("start deleting links older than {} days", conf.getLinkDeletionAfter());
               linkService.deleteLinksOderThan(conf.getLinkDeletionAfter());
               log.info("done deleting links");
            }
            if(conf.getPurgeHistoryAfter() > 0) {
               log.info("start purging history table from records checked before {} days", conf.getPurgeHistoryAfter());
               linkService.purgeHistory(conf.getPurgeHistoryAfter());
               log.info("done purging history");
            }
            if(conf.getPurgeObsoleteAfter() > 0) {
               log.info("start purging obsolete table from records checked before {} days", conf.getPurgeObsoleteAfter());
               linkService.purgeObsolete(conf.getPurgeObsoleteAfter());
               log.info("done purging obsolete");
            }
         }
         
         log.info("end time: {}", LocalDateTime.now());
      };
   }
   
   private void processCollection(Path path, AllCollectionReport allCollectionReport, AllLinkcheckerReport allLinkcheckerReport) throws MalFunctioningProcessorException {

      if(isCollectionRoot(path)) {
         
         log.info("start processing collection from path '{}'", path);
         
         CollectionReport collectionReport = curation.processCollection(path);
         
         allCollectionReport.addReport(collectionReport);
         allLinkcheckerReport.addReport(collectionReport);
         
         storage.saveReport(collectionReport, CurationEntityType.COLLECTION, true);
         
         log.info("done processing collection from path '{}'", path);
         
      }
      else {
         try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path)){
            directoryStream.forEach(dir -> {
                try {
                    processCollection(dir, allCollectionReport, allLinkcheckerReport);
                }
                catch (MalFunctioningProcessorException e) {
                    throw new RuntimeException(e);
                }
            });
         }
         catch (IOException e) {
            log.error("can't create directory stream for path '{}'", path);
            throw new RuntimeException(e);
         }
      }
   }
   
   private boolean isCollectionRoot(Path path) {
      
      try (Stream<Path> stream = Files.walk(path, 1)){
         return stream.anyMatch(Files::isRegularFile);
      }
      catch (IOException e) {
         
         log.error("can't walk through path '{}'", path);
         throw new RuntimeException(e);
      }
   }
}

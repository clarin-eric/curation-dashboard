package eu.clarin.cmdi.curation.app;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import eu.clarin.cmdi.curation.api.CurationModule;
import eu.clarin.cmdi.curation.api.entity.CurationEntityType;
import eu.clarin.cmdi.curation.api.report.collection.AllCollectionReport;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.linkchecker.AllLinkcheckerReport;
import eu.clarin.cmdi.curation.api.report.profile.AllProfileReport;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import eu.clarin.cmdi.curation.api.utils.FileStorage;
import eu.clarin.cmdi.curation.app.conf.AppConfig;
import eu.clarin.cmdi.curation.pph.PPHService;
import eu.clarin.linkchecker.persistence.service.LinkService;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@ComponentScan({"eu.clarin.cmdi.curation", "eu.clarin.linkchecker.persistence"})
@EnableJpaRepositories(basePackages = "eu.clarin.linkchecker.persistence.repository")
@EntityScan(basePackages = "eu.clarin.linkchecker.persistence.model")
@EnableCaching
@EnableConfigurationProperties
@EnableAutoConfiguration
@Slf4j
public class CurationApp {
   
   @Autowired
   private AppConfig conf;   
   @Autowired
   private CurationModule curation;
   @Autowired
   private PPHService pphService;
   @Autowired
   FileStorage storage;
   @Autowired
   LinkService linkService;

	public static void main(String[] args) {
		SpringApplication.run(CurationApp.class, args);
	}
	
   @Bean
   public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
      return args -> {         
         
         if("all".equalsIgnoreCase(conf.getMode()) || "collection".equalsIgnoreCase(conf.getMode())) {
         
        
            final AllCollectionReport allCollectionReport = new AllCollectionReport();
            final AllLinkcheckerReport allLinkcheckerReport = new AllLinkcheckerReport();         
   
            conf.getDirectory().getIn().forEach(inPath -> processCollection(inPath, allCollectionReport,allLinkcheckerReport ));   

            storage.saveReport(allCollectionReport, CurationEntityType.COLLECTION, true);
            storage.saveReport(allLinkcheckerReport, CurationEntityType.LINKCHECKER, true);

         }
         // it's important to process profiles after collections, to fill the collection usage section of the profiles 
         // before they're printed out
         if("all".equalsIgnoreCase(conf.getMode()) || "profile".equalsIgnoreCase(conf.getMode())) {
            
            final AllProfileReport allProfileReport = new AllProfileReport();
   
   
            pphService.getProfileHeaders().forEach(header -> {
               
               log.info("start processing profile '{}'", header.getId());               
   
               try {
                  CMDProfileReport profileReport = curation.processCMDProfile(header.getId());
                  
                  allProfileReport.addReport(profileReport);
                     
                  storage.saveReport(profileReport, CurationEntityType.PROFILE, false);
         
               }
               catch (MalformedURLException e1) {
                  
                  log.error("malformed URL for id '{}' - check the setting for curation.pph-service.restApi");
               }
               
               log.info("done processing profile '{}'", header.getId());
            });
            
            storage.saveReport(allProfileReport, CurationEntityType.PROFILE, false);

         }
         
         if("all".equalsIgnoreCase(conf.getMode()) || "linkchecker".equalsIgnoreCase(conf.getMode())) {
            
            log.info("start writing linkchecker detail reports");
            curation.getLinkcheckerDetailReports().forEach(report -> storage.saveReport(report, CurationEntityType.LINKCHECKER, false));
            log.info("done writing linkchecker detail reports");
 
         }
         
         if("all".equalsIgnoreCase(conf.getMode())) {
            log.info("start deactivating links older than {} days", conf.getLinkDeactivationAfter());
            linkService.deactivateLinksOlderThan(conf.getLinkDeactivationAfter());
            log.info("done deactivating links older than {} days", conf.getLinkDeactivationAfter());
            log.info("start deleting links older than {} days", conf.getLinkDeletionAfter());
            linkService.deleteLinksOderThan(conf.getLinkDeletionAfter());
            log.info("done deleting links older than {} days", conf.getLinkDeletionAfter());
         }
      };
   }
   
   private void processCollection(Path path, AllCollectionReport allCollectionReport, AllLinkcheckerReport allLinkcheckerReport) {      

      if(isCollectionRoot(path)) {
         
         log.info("start processing collection from path '{}'", path);
         
         CollectionReport colectionReport = curation.processCollection(path);
         
         allCollectionReport.addReport(colectionReport);
         allLinkcheckerReport.addReport(colectionReport);
         
         storage.saveReport(colectionReport, CurationEntityType.COLLECTION, true);
         
         log.info("done processing collection from path '{}'", path);
         
      }
      else {
         try {
            Files.newDirectoryStream(path).forEach(dir -> processCollection(dir, allCollectionReport, allLinkcheckerReport));
         }
         catch (IOException e) {
            log.error("can't create directory stream for path '{}'", path);
            throw new RuntimeException(e);
         }
      }
   }
   
   private boolean isCollectionRoot(Path path) {
      
      try {
         return Files.walk(path, 1).anyMatch(fileOrDirectory -> Files.isRegularFile(fileOrDirectory));
      }
      catch (IOException e) {
         
         log.error("can't walk through path '{}'", path);
         throw new RuntimeException(e);
      }
   }
}

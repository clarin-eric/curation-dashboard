package eu.clarin.cmdi.curation.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

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
import eu.clarin.cmdi.curation.api.report.AllPublicProfileReport;
import eu.clarin.cmdi.curation.api.report.Report;
import eu.clarin.cmdi.curation.app.conf.AppConfig;
import eu.clarin.cmdi.curation.pph.PPHService;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "eu.clarin.cmdi.cpa.repository")
@EntityScan(basePackages = "eu.clarin.cmdi.cpa.model")
@EnableCaching
@EnableConfigurationProperties
@ComponentScan("eu.clarin.cmdi")
@EnableAutoConfiguration
@Slf4j
public class CurationApp {
   @Autowired
   AppConfig conf;
   
   @Autowired
   CurationModule curation;
   @Autowired
   PPHService pphService;

   
   @PostConstruct
   public void prepareOutPaths() {
      
      Stream.of("xml", "html").forEach(dir -> {
         
         Stream.of(CurationEntityType.values()).forEach(subdir -> {
            
            Path outDir = conf.getDirectory().getOut().resolve(dir).resolve(subdir.getStringValue());
            
            if(Files.notExists(outDir)) {
               
               try {
                  Files.createDirectories(outDir);
               }
               catch (IOException e) {
                  
                  log.error("can't create output directory '{}'", outDir);
               
               }
            }
            
         });         
      });      
   }

	public static void main(String[] args) {
		SpringApplication.run(CurationApp.class, args);
	}
	
   @Bean
   public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
      return args -> {
         
         final Collection<Report<?>> reports = new ArrayList<Report<?>>();
         
         final AllPublicProfileReport allProfileReport = new AllPublicProfileReport();

         
         reports.clear();
         pphService.getProfileHeaders().forEach(header -> {
            
            Report<?> report = curation.processCMDProfile(header.getId());
            allProfileReport.addReport(report);
            reports.add(report);
         });
         
         
         reports.clear();
         conf.getDirectory().getIn().forEach(inPath -> processCollection(inPath, reports));

      };
   }
   
   private void processCollection(Path path, Collection<Report<?>> reports) {
      
      try {
         if(isCollectionRoot(path)) {
            
            reports.add(curation.processCollection(path));
            
         }
         else {
            processCollection(path, reports);
         }
      }
      catch (IOException e) {
         
         log.error("can't process '{}'", path);
      } 
   }
   
   private boolean isCollectionRoot(Path path) throws IOException {
      
      return Files.walk(path, 1).anyMatch(fileOrDirectory -> Files.isRegularFile(fileOrDirectory));
            
   }
}

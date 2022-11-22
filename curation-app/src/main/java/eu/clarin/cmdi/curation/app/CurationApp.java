package eu.clarin.cmdi.curation.app;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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
import eu.clarin.cmdi.curation.api.exception.SubprocessorException;
import eu.clarin.cmdi.curation.api.report.AllCollectionReport;
import eu.clarin.cmdi.curation.api.report.AllLinkcheckerReport;
import eu.clarin.cmdi.curation.api.report.AllPublicProfileReport;
import eu.clarin.cmdi.curation.api.report.Report;
import eu.clarin.cmdi.curation.api.utils.FileNameEncoder;
import eu.clarin.cmdi.curation.app.conf.AppConfig;
import eu.clarin.cmdi.curation.pph.PPHService;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "eu.clarin.linkchecker.persistence.repository")
@EntityScan(basePackages = "eu.clarin.linkchecker.persistence.model")
@EnableCaching
@EnableConfigurationProperties
@ComponentScan({"eu.clarin.cmdi.curation", "eu.clarin.linkchecker.persistence"})
@EnableAutoConfiguration
@Slf4j
public class CurationApp {
   
   private final LocalDateTime generationDate = LocalDateTime.now();
   
   @Autowired
   private AppConfig conf;   
   @Autowired
   private CurationModule curation;
   @Autowired
   private PPHService pphService;

	public static void main(String[] args) {
		SpringApplication.run(CurationApp.class, args);
	}
	
   @Bean
   public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
      return args -> {
         
         prepareOutPaths();
         
/*         
                 
         final AllPublicProfileReport allProfileReport = new AllPublicProfileReport();


         pphService.getProfileHeaders().forEach(header -> {
            
            Report<?> report;
            try {
               report = curation.processCMDProfile(header.getId());
               
               allProfileReport.addReport(report);
               try {
                  dumpAsXML(report, CurationEntityType.PROFILE);
                  dumpAsHTML(report, CurationEntityType.PROFILE);
               }
               catch (Exception e) {

                  log.error("can't dump report '{}'", report.getName());                
               
               }           
            }
            catch (MalformedURLException e1) {
               
               log.error("malformed URL for id '{}' - check the setting for curation.pph-service.restApi");
            }
            catch (SubprocessorException e1) {

               log.error("can't process public profile with profile id '{}'", header.getId());
            }
         });
         
         try {
            dumpAsXML(allProfileReport, CurationEntityType.PROFILE);
            dumpAsHTML(allProfileReport, CurationEntityType.PROFILE);
         }
         catch (Exception e) {

            log.error("can't dump report '{}'", e, allProfileReport.getName());                
         
         }
         
*/        
         final AllCollectionReport allCollectionReport = new AllCollectionReport();
         final AllLinkcheckerReport allLinkcheckerReport = new AllLinkcheckerReport();         

         conf.getDirectory().getIn().forEach(inPath -> processCollection(inPath, allCollectionReport,allLinkcheckerReport ));

         try {
            dumpAsXML(allCollectionReport, CurationEntityType.COLLECTION);
            dumpAsHTML(allCollectionReport, CurationEntityType.COLLECTION);
         }
         catch (Exception e) {

            log.error("can't dump report '{}'", e, allCollectionReport.getName());                
         
         }
         
         try {
            dumpAsXML(allLinkcheckerReport, CurationEntityType.STATISTICS);
            dumpAsHTML(allLinkcheckerReport, CurationEntityType.STATISTICS);
         }
         catch (Exception e) {

            log.error("can't dump report '{}'", e, allLinkcheckerReport.getName());                
         
         }
      };
   }
   
   private void processCollection(Path path, AllCollectionReport allCollectionReport, AllLinkcheckerReport allLinkcheckerReport) {
      
      try {
         if(isCollectionRoot(path)) {
            
            Report<?> report = curation.processCollection(path);
            
            allCollectionReport.addReport(report);
            allLinkcheckerReport.addReport(report);
            
            dumpAsXML(report, CurationEntityType.COLLECTION);
            dumpAsHTML(report, CurationEntityType.COLLECTION);
            
         }
         else {
            processCollection(path, allCollectionReport, allLinkcheckerReport);
         }
      }

      catch (TransformerException e) {
         
         log.error("", e);
      }
      catch (JAXBException e) {

         log.error("", e);
      }
 
   }
   
   private void prepareOutPaths() {
      
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
   
   private boolean isCollectionRoot(Path path) {
      
      try {
         return Files.walk(path, 1).anyMatch(fileOrDirectory -> Files.isRegularFile(fileOrDirectory));
      }
      catch (IOException e) {
         
         log.error("can't walk through path '{}'", path);
         throw new RuntimeException(e);
      }
   }
   
   private void dumpAsXML(Report<?> report, CurationEntityType type) throws JAXBException {
      
      JAXBContext jc = JAXBContext.newInstance(report.getClass());

      Marshaller marshaller = jc.createMarshaller();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");

      Path path = conf.getDirectory().getOut().resolve("xml").resolve(type.toString());

      String filename = FileNameEncoder.encode(report.getName()) + ".xml";
      path = path.resolve(filename);

      try {
         marshaller.marshal(report, Files.newOutputStream(path));
      }
      catch (IOException e) {
         
         log.error("can't create/write to file '{}'", path);
         throw new RuntimeException(e);
      
      }

      // instead of a rollover we create a copy with report generation date in
      // filename
      copyStampedFile(path);
   
   }

   private void dumpAsHTML(Report<?> report, CurationEntityType type) throws TransformerException, JAXBException {
      
      Path path = this.conf.getDirectory().getOut().resolve("html").resolve(type.toString());

      String filename = FileNameEncoder.encode(report.getName()) + ".html";
      path = path.resolve(filename);

      TransformerFactory factory = TransformerFactory.newInstance();

      Source xslt = new StreamSource(
            this.getClass().getResourceAsStream("/xslt/" + report.getClass().getSimpleName() + "2HTML.xsl"));

      Transformer transformer = factory.newTransformer(xslt);
      transformer.transform(new JAXBSource(JAXBContext.newInstance(report.getClass()), report),
            new StreamResult(path.toFile()));

      copyStampedFile(path);

   }

   private void copyStampedFile(Path path) {
      String stampedFileName = path.getFileName().toString().replace(".",
            "_" + String.format("%1tF", generationDate) + ".");
      Path stampedPath = path.getParent().resolve(stampedFileName);
      try {
         Files.copy(path, stampedPath, StandardCopyOption.REPLACE_EXISTING);
      }
      catch (IOException e) {
         
         log.error("can't copy file '{}' to stmped file '{}'", path, stampedPath);
         throw new RuntimeException(e);
      }
   }
}

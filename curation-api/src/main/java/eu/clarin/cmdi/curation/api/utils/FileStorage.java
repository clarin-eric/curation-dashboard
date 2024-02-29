/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.utils;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.entity.CurationEntityType;
import eu.clarin.cmdi.curation.api.report.NamedReport;
import jakarta.annotation.PostConstruct;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;
import jakarta.xml.bind.util.JAXBSource;
import lombok.extern.slf4j.Slf4j;
import net.sf.saxon.BasicTransformerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;

/**
 * The type File storage.
 */
@Slf4j
@Component
public class FileStorage {

   /**
    * The Conf.
    */
   @Autowired
   ApiConfig conf;

   /**
    * Save report.
    *
    * @param report          the report
    * @param entityType      the entity type
    * @param makeStampedCopy the make stamped copy
    */
   public void saveReport(NamedReport report, CurationEntityType entityType, boolean makeStampedCopy) {
      
      Path xmlFilePath = saveReportAsXML(report, entityType);
      
      Path htmlFilePath = saveReportAsHTML(report, entityType);
      
      
      if(makeStampedCopy) {
         copyStampedFile(xmlFilePath);
         copyStampedFile(htmlFilePath);
      }

   }

   /**
    * Check output path.
    */
   @PostConstruct
   public void checkOutputPath() {
      
      if(conf.getDirectory().getOut() == null && Files.notExists(conf.getDirectory().getOut())) {
         
         log.error("the property 'curation.directory.out' is either not set or the path doesn't exist");
         throw new RuntimeException();
         
      }
   }


   /**
    * Save report as xml path.
    *
    * @param report     the report
    * @param entityType the entity type
    * @return the path
    */
   public Path saveReportAsXML(NamedReport report, CurationEntityType entityType) {
      
      Path outputPath = getOutputPath(report.getName(), entityType, "xml");
      
      Marshaller marshaller = null;
      
      try {
         JAXBContext jc = JAXBContext.newInstance(report.getClass());

         marshaller = jc.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
         marshaller.setProperty(jakarta.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8");
      }
      catch (PropertyException e) {

         log.error("can't set properties for marshaller");
         throw new RuntimeException(e);
      
      }
      catch (JAXBException e) {

         log.error("can't create JAXBContext for class '{}'\n{}", report.getClass(), e);
         
      }
      
      try {
         marshaller.marshal(report, Files.newOutputStream(outputPath));
      }
      catch (IOException e) {
         
         log.error("can't create/write to file '{}'", outputPath);
         throw new RuntimeException(e);
      }
      catch (JAXBException e) {
         
         log.error("can't marshall report '{}'", report.getName());
      }  
      
      return outputPath;
   }

   /**
    * Save report as html path.
    *
    * @param report     the report
    * @param entityType the entity type
    * @return the path
    */
   public Path saveReportAsHTML(NamedReport report, CurationEntityType entityType) {
      
      Path outputPath = getOutputPath(report.getName(), entityType, "html");

      TransformerFactory factory = BasicTransformerFactory.newInstance();
      
      String resourceName = "/xslt/" + report.getClass().getSimpleName() + "2HTML.xsl";

      Source xslt = new StreamSource(
            this.getClass().getResourceAsStream(resourceName));

      Transformer transformer;
      
      try {
         transformer = factory.newTransformer(xslt);
         
         transformer.transform(new JAXBSource(JAXBContext.newInstance(report.getClass()), report),
               new StreamResult(outputPath.toFile()));
      }
      catch (TransformerConfigurationException e) {
         
         log.error("can't load resource '{}'", resourceName);
         throw new RuntimeException(e);
      
      }
      catch (TransformerException|JAXBException e) {

         log.error("can't transforn report '{}'", report.getName());
      
      }
      
      return outputPath;
   }   
   
   private Path getOutputPath(String reportName, CurationEntityType entityType, String fileSuffix) {
      Path outPath = conf.getDirectory().getOut().resolve(fileSuffix).resolve(entityType.toString());
      
      if(Files.notExists(outPath)) {
         try {
            Files.createDirectories(outPath);
         }
         catch (IOException e) {
            
            log.error("can't create directory '{}' - make sure the user has got creation rights", outPath);
            throw new RuntimeException(e);
         
         }
      }

      String filename = FileNameEncoder.encode(reportName) + "." + fileSuffix;
      
      return outPath.resolve(filename);
   }
   
   private void copyStampedFile(Path path) {
      String stampedFileName = path.getFileName().toString().replace(".",
            "_" + String.format("%1tF", LocalDateTime.now()) + ".");
      Path stampedPath = path.getParent().resolve(stampedFileName);
      try {
         Files.copy(path, stampedPath, StandardCopyOption.REPLACE_EXISTING);
      }
      catch (IOException e) {
         
         log.error("can't copy file '{}' to stamped file '{}'", path, stampedPath);
         throw new RuntimeException(e);
      }
   }

}

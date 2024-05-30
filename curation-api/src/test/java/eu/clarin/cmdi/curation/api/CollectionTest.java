/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
@SpringBootTest
@Import(TestConfig.class)
@TestInstance(Lifecycle.PER_CLASS)
@Slf4j
public class CollectionTest {
   
   private final ApiConfig conf;

   private final CurationModule curation; 
   
   private final Path file099Path;
   
   private final Path file099CopyPath;
   
   private final Path collectionPath;
   
   private CMDInstanceReport instanceReport;
   
   private CollectionReport collectionReport; 
   
   String xmlContent;
   
   @Autowired
   public CollectionTest(ApiConfig conf, CurationModule curation) throws IOException {
      

      this.conf = conf;
      
      this.curation = curation;
      
      this.collectionPath = Files.createTempDirectory("collection"); 
      
      this.file099Path = this.collectionPath.resolve("099.xml");
      
      this.file099CopyPath = Files.createTempFile("file099", "xml");
      

   }
   @BeforeAll
   public void init() throws IOException {
      
      // we copy 100 times the reference file into the collection directory but change the file name to a 3 digit numeric value
      // and replace the references to this file with the new file name 
      try(InputStream in = this.getClass().getResourceAsStream("/instance/DE_2009_BergerEtAl_PolitikEntdecken_31_eng.xml")){
         
         this.xmlContent = new String(in.readAllBytes());
         
         IntStream.range(0, 100).forEach(i -> {
            
            String fileName = String.format("%03d", i);
            
            Path filePath = collectionPath.resolve(fileName + ".xml");
            
            try {
               
               Files.writeString(filePath, this.xmlContent.replace("DE_2009_BergerEtAl_PolitikEntdecken_31", fileName), StandardOpenOption.CREATE_NEW);
            }
            catch (IOException e) {
               
               log.error("", e);
            }
         });
         
         Files.copy(file099Path, this.file099CopyPath, StandardCopyOption.REPLACE_EXISTING);
         
         
         // now we create a collection report from our collection
         this.collectionReport = this.curation.processCollection(this.collectionPath);
         
         // and an instance report in collection mode as reference
         this.instanceReport = this.curation.processCMDInstance(collectionPath.resolve("001.xml"));
         
      }
      
      catch(Exception ex) {
         
         log.error("", ex);
         throw new RuntimeException();
      } 
   }
   
   @AfterEach
   void replaceFile099() {
      
      try {
         Files.copy(this.file099CopyPath, file099Path, StandardCopyOption.REPLACE_EXISTING);
      }
      catch (IOException e) {
         
         log.error("", e);
      }
   }
    
   @Test
   void collection() {
      
      assertEquals(collectionPath.getFileName().toString(), this.collectionReport.getName());
      
//      assertEquals(instanceReport.instanceScore * 100, collectionReport.aggregatedScore, 0.1);
      
      assertEquals(instanceReport.instanceScore, this.collectionReport.avgScore, 0.1);
      
      assertEquals(CMDInstanceReport.maxScore * 100 + this.collectionReport.linkcheckerReport.aggregatedMaxScore, collectionReport.aggregatedMaxScore, 0.1);
   } 
   
   @Test
   void file() throws IOException, MalFunctioningProcessorException {
      
      assertEquals(100, this.collectionReport.fileReport.numOfFiles);
      
      assertEquals(0, this.collectionReport.fileReport.numOfFilesNonProcessable);
      
      assertEquals(100, this.collectionReport.fileReport.numOfFilesProcessable);
      
      assertEquals(instanceReport.fileReport.size * 100, this.collectionReport.fileReport.size);
      
      assertEquals(instanceReport.fileReport.score, this.collectionReport.fileReport.avgScore, 0.1);
      
      // now we blow up our file099 to the maximum file size +1
      try(RandomAccessFile raf = new RandomAccessFile(this.file099Path.toFile(), "rw")){
         
         raf.setLength(this.conf.getMaxFileSize() +1);
      };
      
      //now a new local report
      CollectionReport collectionReport = this.curation.processCollection(collectionPath);
      
      assertEquals(100, collectionReport.fileReport.numOfFiles);
      
      assertEquals(1, collectionReport.fileReport.numOfFilesNonProcessable);
      
      assertEquals(99, collectionReport.fileReport.numOfFilesProcessable);
   } 
      

   @Test
   void header() {
      
      assertEquals((instanceReport.instanceHeaderReport.schemaLocation!=null?1:0) * 100,  collectionReport.headerReport.numWithSchemaLocation);
      
      assertEquals((instanceReport.instanceHeaderReport.isCRResident?1:0) * 100,  collectionReport.headerReport.numSchemaCRResident);
      
      assertEquals((instanceReport.instanceHeaderReport.mdProfile!=null?1:0) * 100,  collectionReport.headerReport.numWithMdProfile);
      
      assertEquals((instanceReport.instanceHeaderReport.mdSelfLink!=null?1:0) * 100,  collectionReport.headerReport.numWithMdSelflink);
      
      assertEquals(0, collectionReport.headerReport.duplicatedMDSelfLink.size());
      
      assertEquals((instanceReport.instanceHeaderReport.mdCollectionDisplayName!=null?1:0) * 100,  collectionReport.headerReport.numWithMdCollectionDisplayName);
      
      assertEquals(instanceReport.instanceHeaderReport.score, collectionReport.headerReport.avgScore, 0.1);
      
      //we copy file 000.xml to 099.xml which should create a duplicated self link
      try {
         Files.copy(collectionPath.resolve("000.xml"), file099Path, StandardCopyOption.REPLACE_EXISTING);
         
         CollectionReport collectionReport = this.curation.processCollection(collectionPath);
         
         assertEquals(1, collectionReport.headerReport.duplicatedMDSelfLink.size());
         
         assertEquals(
               "http://worldviews.gei.de/rest/content/cmdi/000/eng/", 
               collectionReport.headerReport.duplicatedMDSelfLink
                  .stream().map(link -> link.mdSelfLink).findFirst().get()
            );
      }
      catch (IOException | MalFunctioningProcessorException e) {
         
         log.error("", e);
      }
   }
    
   @Test
   void resourceProxy() throws IOException, MalFunctioningProcessorException {
      
      assertEquals(instanceReport.resProxyReport.numOfResources *100, collectionReport.resProxyReport.totNumOfResources);
      
      assertEquals(instanceReport.resProxyReport.numOfResources, collectionReport.resProxyReport.avgNumOfResources);
      
      assertEquals(instanceReport.resProxyReport.numOfResourcesWithMime *100, collectionReport.resProxyReport.totNumOfResourcesWithMime);
      
      assertEquals(instanceReport.resProxyReport.numOfResourcesWithMime, collectionReport.resProxyReport.avgNumOfResourcesWithMime);
      
      assertEquals(instanceReport.resProxyReport.numOfResourcesWithReference *100, collectionReport.resProxyReport.totNumOfResourcesWithReference);
      
      assertEquals(instanceReport.resProxyReport.numOfResourcesWithReference, collectionReport.resProxyReport.avgNumOfResourcesWithReference);
      
      assertEquals(0, collectionReport.resProxyReport.invalidReferences.size());
      
      assertEquals(instanceReport.resProxyReport.score, collectionReport.resProxyReport.avgScore, 0.1);
      
      // we add replace file 099 by a file without resources
      this.modifyFile099("Resources.+Resources", "Resources /");
      
      CollectionReport newCollectionReport = this.curation.processCollection(collectionPath);
      
      // we should have one non processable file
      assertEquals(1, newCollectionReport.fileReport.numOfFilesNonProcessable);
      // but the average score should remain
      assertEquals(instanceReport.resProxyReport.score, newCollectionReport.resProxyReport.avgScore, 0.1);
   }   
   
   @Test
   void xMLPopulation() {
      
      assertEquals(instanceReport.xmlPopulationReport.numOfXMLElements, collectionReport.xmlPopulationReport.avgNumOfXMLElements);
      
      assertEquals(instanceReport.xmlPopulationReport.numOfXMLEmptyElements, collectionReport.xmlPopulationReport.avgNumOfXMLEmptyElements);
      
      assertEquals(instanceReport.xmlPopulationReport.numOfXMLSimpleElements, collectionReport.xmlPopulationReport.avgNumOfXMLSimpleElements);
      
      assertEquals(instanceReport.xmlPopulationReport.percOfPopulatedElements, collectionReport.xmlPopulationReport.avgRateOfPopulatedElements);
      
      assertEquals(instanceReport.xmlPopulationReport.score, collectionReport.xmlPopulationReport.avgScore, 0.1);
   }   
   
   @Test
   void xMLValidity() {
      
      assertEquals(100, collectionReport.xmlValidityReport.totNumOfValidRecords);
      
      assertEquals(instanceReport.xmlValidityReport.score, collectionReport.xmlValidityReport.avgScore, 0.1);
   }
   
   @Test
   void facets() {
      
      assertEquals(instanceReport.facetReport.numOfFacets, collectionReport.facetReport.facets.size());
      
      assertEquals(instanceReport.facetReport.score, collectionReport.facetReport.avgScore, 0.1);

   }


//   @Test
   void linkchecking() {
      
      assertEquals(300, collectionReport.linkcheckerReport.totNumOfLinks);
      
      assertEquals(0, collectionReport.linkcheckerReport.totNumOfCheckedLinks);
      
      assertEquals(0.0,collectionReport.linkcheckerReport.aggregatedMaxScore, 0.1);
   }
   
   private void modifyFile099(String regex, String replacement) throws IOException {
      
      String content = Pattern.compile(regex, Pattern.DOTALL).matcher(Files.readString(this.file099CopyPath)).replaceAll(replacement); 
      
      log.debug(content);
      
      Files.writeString(file099Path, content, StandardOpenOption.WRITE);
   }
   
   
   @AfterAll
   public void cleanUp() throws IOException {
      
      Files.walkFileTree(this.collectionPath, new FileVisitor<Path>() {

         @Override
         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

            return FileVisitResult.CONTINUE;
         }

         @Override
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            
            Files.delete(file);
            return FileVisitResult.CONTINUE;
         }

         @Override
         public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {

            return FileVisitResult.TERMINATE;
         }

         @Override
         public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
         }         
      });    
   }
}

/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.report.collection.CollectionReport;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import lombok.extern.slf4j.Slf4j;

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
   
   private Path collectionPath;
   
   private CMDInstanceReport instanceReport;
   
   private CollectionReport collectionReport; 
   
   String xmlContent;
   
   @Autowired
   public CollectionTest(ApiConfig conf, CurationModule curation) throws IOException {
      

      this.conf = conf;
      
      this.curation = curation;
      
      this.collectionPath = Files.createTempDirectory("collection"); 
      

   }
   @BeforeAll
   public void init() throws IOException {
      
      this.conf.setMode("collection");
      
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
         
         
         // now we create a collection report from our collection
         this.collectionReport = this.curation.processCollection(this.collectionPath);
         
         // and an instance report
         this.conf.setMode("instance");
         this.instanceReport = this.curation.processCMDInstance(collectionPath.resolve("001.xml"));
         
         this.conf.setMode("collection");
      }
      
      catch(Exception ex) {
         
         log.error("", ex);
         throw new RuntimeException();
      } 
   }
   
   
   @Test
   void collection() {
      
      assertEquals(collectionPath.getFileName().toString(), collectionReport.getName());     
      
      assertEquals(instanceReport.instanceScore * 100, collectionReport.aggregatedScore, 0.1);
      
      assertEquals(instanceReport.instanceScore, collectionReport.avgScore, 0.1); 
      
      assertEquals(CMDInstanceReport.maxScore * 100 + collectionReport.linkcheckerReport.aggregatedMaxScore, collectionReport.aggregatedMaxScore, 0.1);
   }
  
   
   @Test
   void file() throws IOException {
      
      assertEquals(100, collectionReport.fileReport.numOfFiles);
      
      assertEquals(0, collectionReport.fileReport.numOfFilesNonProcessable);
      
      assertEquals(100, collectionReport.fileReport.numOfFilesProcessable);
      
      assertEquals(instanceReport.fileReport.size * 100, collectionReport.fileReport.size);
      
      assertEquals(instanceReport.fileReport.score, collectionReport.fileReport.avgScore, 0.1);
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

   }
    
   @Test
   void resourceProxy() {
      
      assertEquals(instanceReport.resProxyReport.numOfResources *100, collectionReport.resProxyReport.totNumOfResources);
      
      assertEquals(instanceReport.resProxyReport.numOfResources, collectionReport.resProxyReport.avgNumOfResources);
      
      assertEquals(instanceReport.resProxyReport.numOfResourcesWithMime *100, collectionReport.resProxyReport.totNumOfResourcesWithMime);
      
      assertEquals(instanceReport.resProxyReport.numOfResourcesWithMime, collectionReport.resProxyReport.avgNumOfResourcesWithMime);
      
      assertEquals(instanceReport.resProxyReport.numOfResourcesWithReference *100, collectionReport.resProxyReport.totNumOfResourcesWithReference);
      
      assertEquals(instanceReport.resProxyReport.numOfResourcesWithReference, collectionReport.resProxyReport.avgNumOfResourcesWithReference);
      
      assertEquals(0, collectionReport.resProxyReport.invalidReferences.size());
      
      assertEquals(instanceReport.resProxyReport.score, collectionReport.resProxyReport.avgScore, 0.1);
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

   
   @Test
   void linkchecking() {
      
      assertEquals(300, collectionReport.linkcheckerReport.totNumOfLinks);
      
      assertEquals(0, collectionReport.linkcheckerReport.totNumOfCheckedLinks);
      
      assertEquals(0.0,collectionReport.linkcheckerReport.aggregatedMaxScore, 0.1);
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

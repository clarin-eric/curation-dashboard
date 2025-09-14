package eu.clarin.cmdi.curation.api;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
public class ProfileTest extends BaseTest {
   
   public final String xsdString;
   
   @Autowired
   private CurationModule curation;
   @Autowired
   private ApiConfig conf;
   @Autowired
   private CacheManager cacheManager;

   public ProfileTest() {
      
      try(InputStream in = this.getClass().getResourceAsStream("/profile/teiHeader.xsd")){
         
         this.xsdString = new String(in.readAllBytes());
      }
      
      catch(Exception ex) {
         
         log.error("", ex);
         throw new RuntimeException();
      } 
   }

   
   @Test
   void headerReport() throws URISyntaxException, IOException {
      
      try {
         
         CMDProfileReport report;

         String schemaLocation = "https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1380106710826/xsd";
         
         report = curation.processCMDProfile(schemaLocation);
         //must be true since it's a public profile
         assertTrue(report.headerReport.isPublic());
         
         Path tmpFilePath = Files.createTempFile(null, null);
         
         FileUtils.copyURLToFile(new URL(schemaLocation), tmpFilePath.toFile());
         
         report = curation.processCMDProfile(tmpFilePath);
         // the same profile but uploaded by a user
         assertFalse(report.headerReport.isPublic());
      }
      catch(Exception ex) {
         
         log.error("", ex);
      }     
   }
   
   @Test
   void facetReport() throws URISyntaxException, IOException {

      
      CMDProfileReport report;
      
      try{
         this.cacheManager.getCache("crCache").clear();
         report = curation.processCMDProfile(getTmpFile("",""));        
         assertEquals(9, report.facetReport.numOfFacetsCoveredByProfile);
         
         this.cacheManager.getCache("crCache").clear();
         // removing 'subject' concept
         report = curation.processCMDProfile(getTmpFile("cmd:ConceptLink=\"http://hdl.handle.net/11459/CCR_C-6147_ebed915e-f911-f128-cddc-466aa41c9c73\"", ""));
         assertEquals(8, report.facetReport.numOfFacetsCoveredByProfile);
      }
      catch (IOException e) {

         log.error("", e);
      }   
   }
   
   @Test
   void conceptReport() {

      
      CMDProfileReport report;
      
      try{         
         this.cacheManager.getCache("crCache").clear();
         report = curation.processCMDProfile(getTmpFile("",""));        
         assertEquals(192, report.conceptReport.total);
         assertEquals(190, report.conceptReport.withConcept);
         
         this.cacheManager.getCache("crCache").clear();
         // we delete the concept link, which is four times in the file
         report = curation.processCMDProfile(getTmpFile("cmd:ConceptLink=\"http://hdl.handle.net/11459/CCR_C-6123_009f84ce-e47e-5513-f387-b3502897abae\"", ""));
         // since we haven't deleted any element, the number should have remained the same
         assertEquals(192, report.conceptReport.total);
         // but the number of elements with concept has decreased by nine
         assertEquals(181, report.conceptReport.withConcept);
      }
      catch (IOException e) {

         log.error("", e);
      } 
      
      
   }
   
   @Test
   void cacheTest() throws URISyntaxException, IOException {

      
      try {
         
         // a public profile processed in collection mode should be added to the public cache 
         this.cacheManager.getCache("crCache").clear();
         
         conf.setMode("collection");
         
         String schemaLocation = "https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1::p_1380106710826/xsd";
         
         curation.processCMDProfile(schemaLocation);
         
         assertNotNull(this.cacheManager.getCache("crCache").get(schemaLocation));


         // same public file not loaded via registry URL but by file
         // hence the profile not public, since it doesn't come from the registry, and should be stored in the private cache
         this.cacheManager.getCache("crCache").clear();

         
         Path tmpFilePath = Files.createTempFile(null, null);
         
         FileUtils.copyURLToFile(new URL(schemaLocation), tmpFilePath.toFile());
         
         curation.processCMDProfile(tmpFilePath); 
         
         assertNull(this.cacheManager.getCache("crCache").get("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1403526079380/xsd"));
      }
      catch(Exception ex) {
         
         log.error("", ex);
      }       
   }
   
   
   
   private Path getTmpFile(String replacementPattern, String replacement) throws IOException {
      
      Path tmpFilePath = Files.createTempFile(null, null);
      
      Files.writeString(tmpFilePath, this.xsdString.replace(replacementPattern, replacement));
      
      return tmpFilePath;
   }
}

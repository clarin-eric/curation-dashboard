package eu.clarin.cmdi.curation.api;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.report.profile.CMDProfileReport;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Import(TestConfig.class)
@Slf4j
public class ProfileTest {
   
   public final String xsdString;
   
   @Autowired
   private CurationModule curation;
   @Autowired
   private ApiConfig conf;
   @Autowired
   private CacheManager cacheManager;
   
   public ProfileTest() {
      
      try(InputStream in = this.getClass().getClassLoader().getResourceAsStream("profile/media-corpus-profile.xsd")){
         
         this.xsdString = new String(in.readAllBytes());
      }
      
      catch(Exception ex) {
         
         log.error("", ex);
         throw new RuntimeException();
      } 
   }
   
   @Test
   void headerReport() {
      
      try {
         
         CMDProfileReport report;
         
         URL schemaURL = new URL("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1403526079380/xsd");
         
         report = curation.processCMDProfile(schemaURL);         
         //must be true since it's a public profile
         assertTrue(report.headerReport.isPublic());
         
         Path tmpFilePath = Files.createTempFile(null, null);
         
         FileUtils.copyURLToFile(schemaURL, tmpFilePath.toFile());
         
         report = curation.processCMDProfile(tmpFilePath);
         // the same profile but uploaded by a user
         assertFalse(report.headerReport.isPublic());
      }
      catch(Exception ex) {
         
         log.error("", ex);
      }     
   }
   
   @Test
   void facetReport() {
      
      CMDProfileReport report;
      
      try{         
         this.cacheManager.getCache("privateProfileCache").clear();  
         report = curation.processCMDProfile(getTmpFile("",""));        
         assertEquals(conf.getFacets().size(), report.facetReport.numOfFacetsCoveredByProfile);
         
         this.cacheManager.getCache("privateProfileCache").clear();         
         report = curation.processCMDProfile(getTmpFile("cmd:ConceptLink=\"http://hdl.handle.net/11459/CCR_C-2571_2be2e583-e5af-34c2-3673-93359ec1f7df\"", ""));
         assertEquals(conf.getFacets().size() -1, report.facetReport.numOfFacetsCoveredByProfile); 
      }
      catch (IOException e) {

         log.error("", e);
      }   
   }
   
   @Test
   void conceptReport() {
      
      CMDProfileReport report;
      
      try{         
         this.cacheManager.getCache("privateProfileCache").clear();  
         report = curation.processCMDProfile(getTmpFile("",""));        
         assertEquals(101, report.conceptReport.total);
         assertEquals(88, report.conceptReport.withConcept);
         
         this.cacheManager.getCache("privateProfileCache").clear();   
         // we delete the concept link, which is four times in the file
         report = curation.processCMDProfile(getTmpFile("cmd:ConceptLink=\"http://hdl.handle.net/11459/CCR_C-63_95ec8724-267a-8689-a04d-50ae515bbacf\"", ""));
         // since we haven't deleted any element, the number should have remained the same
         assertEquals(101, report.conceptReport.total);
         // but the number of elements with concept has decreased by four
         assertEquals(84, report.conceptReport.withConcept); 
      }
      catch (IOException e) {

         log.error("", e);
      } 
      
      
   }
   
   
   
   private Path getTmpFile(String replacementPattern, String replacement) throws IOException {
      
      Path tmpFilePath = Files.createTempFile(null, null);
      
      Files.writeString(tmpFilePath, this.xsdString.replace(replacementPattern, replacement));
      
      return tmpFilePath;
   }  
}

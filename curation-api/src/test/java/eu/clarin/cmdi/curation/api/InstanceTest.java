/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import lombok.extern.slf4j.Slf4j;

/**
 *
 */
@SpringBootTest
@Import(TestConfig.class)
@Slf4j
public class InstanceTest {
   
   private final String cmdString;
   
   private final CMDInstanceReport referenceReport;   
   
   private final ApiConfig conf;

   private final CurationModule curation;
   
   @Autowired
   public InstanceTest(ApiConfig conf, CurationModule curation) {
      
      conf.setMode("instance");
      this.conf = conf;
      
      this.curation = curation;
      
      try(InputStream in = this.getClass().getResourceAsStream("/instance/DE_2009_BergerEtAl_PolitikEntdecken_31_eng.xml")){
         
         this.cmdString = new String(in.readAllBytes());
         
         this.referenceReport = curation.processCMDInstance(Paths.get(this.getClass().getResource("/instance/DE_2009_BergerEtAl_PolitikEntdecken_31_eng.xml").toURI()));
         
      }
      
      catch(Exception ex) {
         
         log.error("", ex);
         throw new RuntimeException();
      } 
   }
   
   @Test
   void file() {
      
      assertEquals(11139, this.referenceReport.fileReport.size);
      assertTrue(this.referenceReport.fileReport.size <= conf.getMaxFileSize());
      assertEquals(1.0, this.referenceReport.fileReport.score);
      
      // now we add a comment at the end which is a bit larger than the maximal file 
      // therefore the file size becomes in any way larger than the maximal file size
      
      Path tmpFilePath = null;  
      
      try (BufferedWriter writer = Files.newBufferedWriter((tmpFilePath = getTmpFile("", "")), StandardOpenOption.APPEND)){
         
         writer.write("<!-- ");
         
         IntStream.range(0, conf.getMaxFileSize()).forEach(i -> {
            try {
               writer.write('c');
            }
            catch (IOException e) {
               
               log.error("", e);
            }
         });
         
         writer.write(" -->");
         
      }
      catch(Exception ex) {
         
         log.error("", ex);
      } 
      
      CMDInstanceReport report = curation.processCMDInstance(tmpFilePath);
      
      assertTrue(report.fileReport.size > conf.getMaxFileSize());
      assertFalse(report.isProcessable);
   }
   

   @Test
   void instanceHeader() {
      
      try {
         
         assertEquals(5.0, referenceReport.instanceHeaderReport.score);
         
         CMDInstanceReport report = curation.processCMDInstance(getTmpFile("clarin.eu:cr1:p_1380106710826","clarin.eu:cr1:p1380106710826"));
         assertFalse(report.isProcessable);
         
         report = curation.processCMDInstance(getTmpFile("<cmd:MdProfile>clarin.eu:cr1:p_1380106710826</cmd:MdProfile>","<cmd:MdProfile>eu:cr1:p_1380106710826</cmd:MdProfile>"));
         assertEquals(4.0, report.instanceHeaderReport.score);
         
         report = curation.processCMDInstance(getTmpFile("<cmd:MdCollectionDisplayName>WorldViews</cmd:MdCollectionDisplayName>",""));
         assertEquals(4.0, report.instanceHeaderReport.score);
         
         report = curation.processCMDInstance(getTmpFile("<cmd:MdSelfLink>http://worldviews.gei.de/rest/content/cmdi/DE_2009_BergerEtAl_PolitikEntdecken_31/eng/</cmd:MdSelfLink>",""));
         assertEquals(4.0, report.instanceHeaderReport.score);
         
      }
      catch (IOException e) {
         
         log.error("", e);
      } 
   }
   
   @Test
   void resourceProxy() {
      
      assertEquals(2, referenceReport.resProxyReport.numOfResourcesWithMime);
      assertEquals(2, referenceReport.resProxyReport.numOfResourcesWithReference);
      assertEquals(1.0, referenceReport.resProxyReport.percOfResourcesWithMime);
      assertEquals(1.0, referenceReport.resProxyReport.percOfResourcesWithReference);
      assertEquals(2.0, referenceReport.resProxyReport.score);

      
      try {
         // delete cmd:Resources element - (?s).* is the DOTALL flag which includes line feed
         // files without resources should be treated as non processable
         CMDInstanceReport report = curation.processCMDInstance(getTmpFile("<cmd:Resources>(?s).*</cmd:Resources>",""));
         assertFalse(report.isProcessable);
         // should be the same if we have no resource reference
         report = curation.processCMDInstance(getTmpFile("<cmd:ResourceRef>.*</cmd:ResourceRef>",""));
         assertFalse(report.isProcessable);
         
         // deleting all mime-types
         report = curation.processCMDInstance(getTmpFile("mimetype=\".*\"",""));
         assertEquals(0, report.resProxyReport.numOfResourcesWithMime);
         assertEquals(2, report.resProxyReport.numOfResourcesWithReference);
         assertEquals(0.0, report.resProxyReport.percOfResourcesWithMime);
         assertEquals(1.0, report.resProxyReport.percOfResourcesWithReference);
         assertEquals(1.0, report.resProxyReport.score);         
         
      }
      catch (IOException e) {
         
         log.error("", e);
      }      
   }
   
   @Test
   void XMLPopulation() {
      
      try {
         
         // deleting a simple element
         CMDInstanceReport report = curation.processCMDInstance(getTmpFile("<cmdp:repository>.*</cmdp:repository>",""));
         
         assertEquals(referenceReport.xmlPopulationReport.numOfXMLElements -1,  report.xmlPopulationReport.numOfXMLElements);     
         assertEquals(referenceReport.xmlPopulationReport.numOfXMLSimpleElements -1,  report.xmlPopulationReport.numOfXMLSimpleElements);
         assertEquals(referenceReport.xmlPopulationReport.numOfXMLEmptyElements,  report.xmlPopulationReport.numOfXMLEmptyElements);
         
         // deleting a complex element
         report = curation.processCMDInstance(getTmpFile("<cmdp:publicationStmt(?s).*</cmdp:publicationStmt>",""));
         assertEquals(referenceReport.xmlPopulationReport.numOfXMLElements -9,  report.xmlPopulationReport.numOfXMLElements);     
         assertEquals(referenceReport.xmlPopulationReport.numOfXMLSimpleElements -7,  report.xmlPopulationReport.numOfXMLSimpleElements);
         
         // deleting the content of a simple element 
         report = curation.processCMDInstance(getTmpFile("<cmdp:num type=\"pages\" n=\"1\">31</cmdp:num>","<cmdp:num type=\"pages\" n=\"1\" />"));
         assertEquals(referenceReport.xmlPopulationReport.numOfXMLEmptyElements +1,  report.xmlPopulationReport.numOfXMLEmptyElements);
         
      }
      catch (IOException e) {
         
         log.error("", e);
      } 
   }
   
   @Test
   void XMLValidity() {
      
      // less than 3 warnings with severity FATAL or ERROR lead to a score 1.0
      assertEquals(1.0, referenceReport.xmlValidityReport.score);
      
      try {
         // we create more than 3 ERRORS, since the Attribute 'x' is not allowed to appear in element 'cmdp:item'
         CMDInstanceReport report = curation.processCMDInstance(getTmpFile("<cmdp:item n","<cmdp:item x"));
         
         assertEquals(0.0, report.xmlValidityReport.score);
         
      }
      catch (IOException e) {
         
         log.error("", e);
      }      
   }
   
   @Test
   void facets() {
      
      // assuring that any defined facet is in the coverage list
      conf.getFacets().forEach(definedFacetName -> {
         assertTrue(this.referenceReport.facetReport.coverages.stream().anyMatch(coverage -> coverage.name.equals(definedFacetName)));
      });
      
      try {        
         
         assertTrue(referenceReport.facetReport.coverages.stream().filter(coverage -> coverage.name.equals("resourceClass")).findFirst().get().coveredByInstance);
         
         CMDInstanceReport report = curation.processCMDInstance(getTmpFile("<cmdp:type>.*</cmdp:type>", ""));
         // we delete the elements who are linked the resourceClass facet
         assertFalse(report.facetReport.coverages.stream().filter(coverage -> coverage.name.equals("resourceClass")).findFirst().get().coveredByInstance);
         assertEquals(referenceReport.facetReport.numOfFacetsCoveredByInstance -1 , report.facetReport.numOfFacetsCoveredByInstance);
         
         assertTrue(referenceReport.facetReport.coverages.stream().filter(coverage -> coverage.name.equals("organisation")).findFirst().get().coveredByInstance);
         
         // repeating procedure for organisation facet
         // we delete the elements who are linked the organisation facet
         report = curation.processCMDInstance(getTmpFile("<cmdp:publisher>.*</cmdp:publisher>", ""));

         assertFalse(report.facetReport.coverages.stream().filter(coverage -> coverage.name.equals("organisation")).findFirst().get().coveredByInstance);
         assertEquals(referenceReport.facetReport.numOfFacetsCoveredByInstance -1 , report.facetReport.numOfFacetsCoveredByInstance);
         
      }
      catch (IOException e) {
         
         log.error("", e);
      }
   }
   
   /*
    * there is no URL validation on the instance level since individual checks would take to long and we don't want 
    * to unauthenticated the mean to write rubbish to the linkchecker database
    */
   
   private Path getTmpFile(String replacementPattern, String replacement) throws IOException {
      
      Path tmpFilePath = Files.createTempFile(null, null);
      
      Files.writeString(tmpFilePath, this.cmdString.replaceAll(replacementPattern, replacement));
      
      return tmpFilePath;
   }  

}

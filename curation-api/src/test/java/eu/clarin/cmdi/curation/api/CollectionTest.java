/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import eu.clarin.cmdi.curation.api.conf.ApiConfig;
import eu.clarin.cmdi.curation.api.report.CollectionReport;
import eu.clarin.cmdi.curation.api.report.Report;

/**
 *
 */
@SpringBootTest
@Import(TestConfig.class)
@TestInstance(Lifecycle.PER_CLASS)
public class CollectionTest {
   
   private Report<?> report;
   
   @Autowired
   CurationModule curation;
   @Autowired
   ApiConfig config;
   
   @BeforeAll
   public void createInstanceReport() throws URISyntaxException {
      
      config.setMode("collection");
      
      URI instanceURI = this.getClass().getClassLoader().getResource("instance/SAW_Leipzig_Repository").toURI();
      Path collectionPath = Paths.get(instanceURI);
      
      this.report = curation.processCollection(collectionPath);
      
   }
   
   @Test
   @Order(1)
   public void isColletionReport() {
      
      assertTrue(this.report instanceof CollectionReport);
      
      this.report.toXML(System.out);
      
   }
   
   @Test
   @Order(2)
   public void sectionReportNotNull() {
      
      CollectionReport collectionReport = CollectionReport.class.cast(this.report);
      
      assertNotNull(collectionReport.fileReport);
      
      assertNotNull(collectionReport.headerReport);
      
      assertNotNull(collectionReport.resProxyReport);
      
      assertNotNull(collectionReport.urlReport);
      
      assertNotNull(collectionReport.xmlPopulatedReport);
      
      assertNotNull(collectionReport.xmlValidationReport);
      
   }
}

package eu.clarin.cmdi.curation.api;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.MalformedURLException;
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

import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.Report;

@SpringBootTest
@Import(TestConfig.class)
@TestInstance(Lifecycle.PER_CLASS)
public class ProfileTest {
   
   private Report<?> report;
   
   @Autowired
   CurationModule curation;
   
   @BeforeAll
   public void createReport() throws URISyntaxException, MalformedURLException {
      
      URI profileURI = this.getClass().getClassLoader().getResource("profile/LexicalResourceProfile.xsd").toURI();
      Path profilePath = Paths.get(profileURI);
      
      this.report = curation.processCMDProfile(profilePath);
   }
   
   @Test
   @Order(1)
   public void isProfileReport() throws URISyntaxException, MalformedURLException {
       
      assertTrue(report instanceof CMDProfileReport);
      
   }
   
   @Test
   @Order(2)
   public void sectionReportsNotNull() {
        
      CMDProfileReport profileReport = CMDProfileReport.class.cast(report);
      
      assertNotNull(profileReport.header);
      
      assertNotNull(profileReport.components);
      
      assertNotNull(profileReport.elements);
      
      assertNotNull(profileReport.facet);
      
      
      //report.toXML(System.out);
      
   };
}

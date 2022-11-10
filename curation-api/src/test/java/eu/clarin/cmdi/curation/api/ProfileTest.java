package eu.clarin.cmdi.curation.api;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import eu.clarin.cmdi.curation.api.report.CMDProfileReport;
import eu.clarin.cmdi.curation.api.report.Report;

@SpringBootTest
@Import(TestConfig.class)

public class ProfileTest {
   
   @Autowired
   CurationModule curation;
   
   @Test
   public void createProfileReport() throws URISyntaxException, MalformedURLException {
      
      URI profileURI = this.getClass().getClassLoader().getResource("profile/LexicalResourceProfile.xsd").toURI();
      Path profilePath = Paths.get(profileURI);
      
      Report<?> report = curation.processCMDProfile(profilePath);
      
      assertTrue(report instanceof CMDProfileReport);
      
      CMDProfileReport profileReport = CMDProfileReport.class.cast(report);
      
      assertNotNull(profileReport.header);
      
      
      //report.toXML(System.out);
      
   };
}

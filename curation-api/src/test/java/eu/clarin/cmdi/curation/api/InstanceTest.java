/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import eu.clarin.cmdi.curation.api.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.Report;

/**
 *
 */
@SpringBootTest
@Import(TestConfig.class)
public class InstanceTest {
   
   @Autowired
   CurationModule curation;
   
   @Test
   public void createInstanceReport() throws URISyntaxException {
      
      URI instanceURI = this.getClass().getClassLoader().getResource("instance/SAW_Leipzig_Repository/cts_muqtabas_urn_cts_muqtabas_oclc_4770057679_i_29_TEIP5_.xml").toURI();
      Path instancePath = Paths.get(instanceURI);
      
      Report<?> report = curation.processCMDInstance(instancePath);
      
      assertTrue(report instanceof CMDInstanceReport);
      
      report.toXML(System.out);
      
   }

}

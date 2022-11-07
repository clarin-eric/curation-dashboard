package eu.clarin.cmdi.curation.api;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import eu.clarin.cmdi.curation.api.report.Report;

@SpringBootTest
@EnableConfigurationProperties
@ComponentScan("eu.clarin.cmdi")
@EnableAutoConfiguration
@Import(CurationTest.TestConfig.class)
@EnableCaching
public class CurationTest {
   
   @Autowired
   CurationModule curation;
   
   @Test
   public void createProfileReport() throws URISyntaxException, MalformedURLException {
      
      URI profileURI = this.getClass().getClassLoader().getResource("profile/LexicalResourceProfile.xsd").toURI();
      Path profilePath = Paths.get(profileURI);
      
      Report<?> report = curation.processCMDProfile(profilePath);
      
      report.toXML(System.out);
      
   };
   
   @SpringBootConfiguration
   @EnableJpaRepositories(basePackages = "eu.clarin.cmdi.cpa.repository")
   @EntityScan(basePackages = "eu.clarin.cmdi.cpa.model")
   public static class TestConfig{
   }
}

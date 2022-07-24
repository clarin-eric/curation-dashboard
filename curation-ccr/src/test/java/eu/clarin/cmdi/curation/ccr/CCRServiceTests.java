package eu.clarin.cmdi.curation.ccr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest()
@Import(CCRServiceTests.TestConfig.class)
@EnableConfigurationProperties
class CCRServiceTests {
   
   @Autowired
   CCRServiceFactory fac;

	@Test
	void getCCRService() {
	   
	   CCRService service = fac.getCCRService();
	   
	   assertTrue(service.getAll().size() > 0);
	}
	
	@SpringBootConfiguration	
	@ComponentScan
	public static class TestConfig {
	}

}

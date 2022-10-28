package eu.clarin.cmdi.curation.cr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import eu.clarin.cmdi.curation.cr.conf.CRProperties;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import eu.clarin.cmdi.curation.pph.PPHService;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest()
@EnableCaching
//@Import(TestConfig.class)
@EnableConfigurationProperties
@EnableAutoConfiguration
@ComponentScan(basePackages = "eu.clarin.cmdi.curation")
@Slf4j
class CRServiceTests {
   
   @Autowired
   CRServiceFactory fac;
   
   @Autowired
   CRProperties crProps;
   
   @Autowired
   PPHService phService;

	@Test
	void getCRService() {
	   

	   
	   CRService service = fac.getCRService();
	   
	   phService.getProfileHeaders().stream().limit(5).forEach(header -> {

	      try {
            ParsedProfile profile = service.getParsedProfile(header);
            
            assertEquals(profile.getId(), header.getId()); 
         }
         catch (Exception e) {
            log.error("error in schema '{}'", header.getId());
            log.error("", e);
         }
	   });   
	}
}

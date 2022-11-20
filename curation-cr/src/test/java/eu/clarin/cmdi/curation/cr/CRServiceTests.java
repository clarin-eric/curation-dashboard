package eu.clarin.cmdi.curation.cr;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import eu.clarin.cmdi.curation.cr.conf.CRConfig;
import eu.clarin.cmdi.curation.pph.PPHService;
import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest()
@Import(CRServiceTests.TestConfig.class)
@EnableConfigurationProperties
@EnableAutoConfiguration
@ComponentScan(basePackages = "eu.clarin.cmdi.curation")
@Slf4j
class CRServiceTests {
   
   @Autowired
   CRService crService;   
   @Autowired
   CRConfig crProps;  
   @Autowired
   PPHService phService;
   @Autowired
   CacheManager cacheManager;

	@Test
	void getCRService() {

	   
	   phService.getProfileHeaders().stream().limit(5).forEach(header -> {

	      try {
            
            assertEquals(crService.getParsedProfile(header).getId(), header.getId()); 
         
	      }
         catch (Exception e) {
            log.error("error in schema '{}'", header.getId());
            log.error("", e);
         }
	   });   
	}
	
	@Test
	void useUache() {
	   
      phService.getProfileHeaders().stream().limit(5).forEach(header -> {

         try {
            crService.getParsedProfile(header);
            
            assertNotNull(cacheManager.getCache("publicProfileCache").get(header.getId()));
             
         }
         catch (Exception e) {
            log.error("error in schema '{}'", header.getId());
            log.error("", e);
         }
      });  	   
	   
	}
	
	@SpringBootConfiguration
	@EnableCaching
	public static class TestConfig {

	}
}

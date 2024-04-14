package eu.clarin.cmdi.curation.ccr;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import org.junit.jupiter.api.Assertions;
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

@SpringBootTest
@EnableConfigurationProperties
@EnableAutoConfiguration
@ComponentScan
@Import(CCRServiceTests.TestConfig.class)
@EnableCaching
class CCRServiceTests {

   @Autowired
   CCRService service;

   @Autowired
   CacheManager cacheManager;
   
   @Test
   void useCache() throws CCRServiceNotAvailableException {

      service.getConcept("http://hdl.handle.net/11459/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c");

      Assertions.assertNotNull(cacheManager.getCache("ccrCache").get("http://hdl.handle.net/11459/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c"));

   }

   @SpringBootConfiguration
   @ComponentScan(basePackages = "eu.clarin.cmdi.curation")
   public static class TestConfig {

   }
}

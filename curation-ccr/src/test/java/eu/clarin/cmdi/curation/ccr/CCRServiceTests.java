package eu.clarin.cmdi.curation.ccr;

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

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest()
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
   void getAll() {

      assertTrue(service.getAll().size() > 0);

      assertTrue(cacheManager.getCache("ccrCache").get("getCCRConceptMap") != null);

   }
   
   @Test
   void useCache() {

      service.getAll();

      assertTrue(cacheManager.getCache("ccrCache").get("getCCRConceptMap") != null);

   }

   @SpringBootConfiguration

   public static class TestConfig {

   }

}

package eu.clarin.cmdi.curation.ph;

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

import eu.clarin.cmdi.curation.pph.PPHService;
import eu.clarin.cmdi.curation.pph.cache.PPHCache;
import eu.clarin.cmdi.curation.pph.conf.PHProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest()
@Import(ProfileHeadersTests.TestConfig.class)
@EnableConfigurationProperties
@EnableAutoConfiguration
class ProfileHeadersTests {

   @Autowired
   PHProperties crProps;
   @Autowired
   PPHService service;
   @Autowired
   PPHCache pphCache;
   @Autowired
   CacheManager cacheManager;

   @Test
   void createPublicProfiles() {

      assertEquals(
            service.getProfileHeaders().stream()
                  .filter(header -> header.getStatus().equals("deprecated")).count(),
            pphCache.getProfileHeadersMap(crProps.getRestApi(), "?status=deprecated").size());

      assertEquals(
            service.getProfileHeaders().stream()
                  .filter(header -> header.getStatus().equals("development")).count(),
                  pphCache.getProfileHeadersMap(crProps.getRestApi(), "?status=development").size());

   }
   @Test
   void useCache() {
      service.getProfileHeaders();
      
      assertNotNull(cacheManager.getCache("pphCache").get(crProps.getQuery()));
   }
   
   @SpringBootConfiguration   
   @ComponentScan(basePackages = "eu.clarin.cmdi.curation")
   @EnableCaching
   public static class TestConfig {

   }
}

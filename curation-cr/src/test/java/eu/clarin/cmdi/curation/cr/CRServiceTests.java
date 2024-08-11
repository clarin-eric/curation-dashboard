package eu.clarin.cmdi.curation.cr;

import eu.clarin.cmdi.curation.cr.conf.CRConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockserver.client.MockServerClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import javax.net.ssl.HttpsURLConnection;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest()
@Import(CRServiceTests.TestConfig.class)
@EnableConfigurationProperties
@EnableAutoConfiguration
@ComponentScan(basePackages = "eu.clarin.cmdi.curation")
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CRServiceTests {
   
   @Autowired
   CRService crService;   
   @Autowired
   CRConfig crConfig;
   @Autowired
   CacheManager cacheManager;

    @BeforeAll
    public void prepareFileCache() throws IOException {

        FileUtils.copyDirectory(new File(this.getClass().getResource("/cr_cache").getFile()), crConfig.getCrCache().toFile());
    }

	@Test
    @Order(1)
	void isPublic()  {

	      try {
            
            assertTrue(crService.getParsedProfile("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1403526079381/xsd").header().isPublic() );
//            assertFalse(crService.getParsedProfile("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1403526079382/xsd").header().isPublic() );
	      }
         catch (Exception e) {

            log.error("error in schema");
            log.error("", e);
         }
	}
	
	@Test
    @Order(2)
	void cacheUsage() {

         try {
            
//            assertNotNull(cacheManager.getCache("profileCache").get("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1403526079381/xsd"));
//            assertNotNull(cacheManager.getCache("profileCache").get("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1403526079382/xsd"));

         }
         catch (Exception e) {

            log.error("error in schema");
            log.error("", e);
         }
	}
	
	@SpringBootConfiguration
	@EnableCaching
	public static class TestConfig {

	}
}

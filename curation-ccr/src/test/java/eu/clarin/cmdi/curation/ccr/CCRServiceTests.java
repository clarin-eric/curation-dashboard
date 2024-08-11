package eu.clarin.cmdi.curation.ccr;

import eu.clarin.cmdi.curation.ccr.conf.CCRConfig;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@SpringBootTest
@EnableConfigurationProperties
@EnableAutoConfiguration
@ComponentScan
@Import(CCRServiceTests.TestConfig.class)
@EnableCaching
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CCRServiceTests {

   @Autowired
   CCRService service;

   @Autowired
   CacheManager cacheManager;

   @Autowired
   CCRConfig ccrConfig;

   @BeforeAll
   public void prepareFileCache() throws IOException {

      FileUtils.copyDirectory(new File(this.getClass().getResource("/ccr_cache").getFile()), ccrConfig.getCcrCache().toFile());
   }

   
   @Test
   void useCache() throws CCRServiceNotAvailableException {

      service.getConcept("http://hdl.handle.net/11459/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c");

      // getCache(...).get(...) returns only a value wrapper, the following get the value iteself
      Assertions.assertNotNull(cacheManager.getCache("ccrCache").get("http://hdl.handle.net/11459/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c").get());
   }

   @SpringBootConfiguration
   @ComponentScan(basePackages = "eu.clarin.cmdi.curation")
   public static class TestConfig {

   }
}

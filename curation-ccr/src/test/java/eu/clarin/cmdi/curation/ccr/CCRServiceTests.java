package eu.clarin.cmdi.curation.ccr;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockserver.client.MockServerClient;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.socket.tls.KeyStoreFactory;
import org.mockserver.springtest.MockServerPort;
import org.mockserver.springtest.MockServerTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import javax.net.ssl.HttpsURLConnection;

@SpringBootTest
@EnableConfigurationProperties
@EnableAutoConfiguration
@ComponentScan
@Import(CCRServiceTests.TestConfig.class)
@EnableCaching
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MockServerTest
class CCRServiceTests {

   @Autowired
   private CCRService service;

   @Autowired
   private CacheManager cacheManager;


   private MockServerClient mockServerClient;

   @MockServerPort
   private Integer mockServerPort;



   @BeforeAll
   public void prepareFileCache() throws IOException {
      // ensure all connection using HTTPS will use the SSL context defined by
      // MockServer to allow dynamically generated certificates to be accepted
      HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(new MockServerLogger()).sslContext().getSocketFactory());

   }
   @Test
   void serverNotAvailable() throws CCRServiceNotAvailableException {


      this.mockServerClient
              .when(
                  request()
               )
              .respond(
                  response().withStatusCode(503)
              );


      // shouldn't throw any exception
      assertDoesNotThrow(() -> service.getConcept("http://hdl.handle.net/99999/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c"));
      // should return null
      assertNull(service.getConcept("http://hdl.handle.net/99999/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c"));
      // the null should be cached
      Assertions.assertNotNull(cacheManager.getCache("ccrCache").get("http://hdl.handle.net/99999/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c"));
      // the cache value should be null
      Assertions.assertNull(cacheManager.getCache("ccrCache").get("http://hdl.handle.net/99997/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c").get());
   }

   @Test
   void connectionTimeout() throws CCRServiceNotAvailableException {

      this.mockServerClient
              .when(
                      request()
              )
              .respond(
                      response()
                              .withStatusCode(200)
                              .withDelay(TimeUnit.SECONDS, 6)
              );

      // shouldn't throw any exception
      assertDoesNotThrow(() -> service.getConcept("http://hdl.handle.net/99998/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c"));
      // should return null
      assertNull(service.getConcept("http://hdl.handle.net/99998/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c"));
      // the null should be cached
      Assertions.assertNotNull(cacheManager.getCache("ccrCache").get("http://hdl.handle.net/99998/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c"));
      // the cache value should be null
      Assertions.assertNull(cacheManager.getCache("ccrCache").get("http://hdl.handle.net/99998/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c").get());   }

   @Test
   void nonParseableResult() throws CCRServiceNotAvailableException {

      this.mockServerClient
              .when(
                      request()
              )
              .respond(
                      response()
                              .withBody("")
              );
      // shouldn't throw any exception for a non parseable result
      assertDoesNotThrow(() -> service.getConcept("http://hdl.handle.net/99997/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c"));
      // should return null
      assertNull(service.getConcept("http://hdl.handle.net/99997/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c"));
      // the null should be cached
      Assertions.assertNotNull(cacheManager.getCache("ccrCache").get("http://hdl.handle.net/99997/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c"));
      // the cache value should be null
      Assertions.assertNull(cacheManager.getCache("ccrCache").get("http://hdl.handle.net/99997/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c").get());
   }

   @Test
   void useCache() throws CCRServiceNotAvailableException {

      service.getConcept("http://hdl.handle.net/11459/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c");

      // getCache(...).get(...) returns only a value wrapper, the following get the value iteself
      Assertions.assertNotNull(cacheManager.getCache("ccrCache").get("http://hdl.handle.net/11459/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c").get());
   }

   @SpringBootConfiguration
   @EnableCaching
   @ComponentScan(basePackages = "eu.clarin.cmdi.curation")
   public static class TestConfig {

   }
}

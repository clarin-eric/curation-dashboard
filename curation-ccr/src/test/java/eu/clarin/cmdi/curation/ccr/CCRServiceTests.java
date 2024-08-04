package eu.clarin.cmdi.curation.ccr;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockserver.client.MockServerClient;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.socket.tls.KeyStoreFactory;
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

import javax.net.ssl.HttpsURLConnection;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@EnableConfigurationProperties
@EnableAutoConfiguration
@ComponentScan
@Import(CCRServiceTests.TestConfig.class)
@EnableCaching
@MockServerTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CCRServiceTests {

   @Autowired
   CCRService service;

   @Autowired
   CacheManager cacheManager;

   private MockServerClient mockServerClient;

   @BeforeAll
   public void setExpectations(){
      // necessary for SSL (see https://www.mock-server.com/mock_server/HTTPS_TLS.html)
      HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(new MockServerLogger()).sslContext().getSocketFactory());

      this.mockServerClient.when(
              request().withPath("/clavas/rest/v1/data")
      ).respond(
              response()
                      .withBody(
                              """
                             <?xml version="1.0" encoding="utf-8" ?>
                             <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                                      xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                                      xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
                                      xmlns:ns0="http://openskos.org/xmlns#">
                             <skos:ConceptScheme rdf:about="http://hdl.handle.net/11459/CCR_P-Metadata_6f3f84d1-6f06-6291-4e20-4cd361cca128">
                             <rdfs:label xml:lang="en">Metadata</rdfs:label>
                             <skos:hasTopConcept>
                             <skos:Concept rdf:about="http://hdl.handle.net/11459/CCR_C-4541_6dc2d021-1811-3d05-56ca-7ef3e394817c">
                             <skos:prefLabel xml:lang="en">mixed design</skos:prefLabel>
                             <skos:definition xml:lang="en">
                             Indication of the design type used for the elicitation of experimental data as a mixed design. (source: NaLiDa)
                             </skos:definition>
                             <skos:inScheme rdf:resource="http://hdl.handle.net/11459/CCR_P-Metadata_6f3f84d1-6f06-6291-4e20-4cd361cca128"/>
                             <skos:example xml:lang="en">
                             This type of design can be found, for instance, in experimental studies or, more specific, in the field of psychology, psycholinguistics, medicine, etc. (source: NaLiDa)
                             </skos:example>
                             <skos:altLabel>mixedDesign</skos:altLabel>
                             <skos:altLabel>mixed design</skos:altLabel>
                             <skos:topConceptOf rdf:resource="http://hdl.handle.net/11459/CCR_P-Metadata_6f3f84d1-6f06-6291-4e20-4cd361cca128"/>
                             <skos:changeNote>
                             This concept is based on the ISOcat data category: http://www.isocat.org/datcat/DC-4541
                             </skos:changeNote>
                             <skos:notation>mixedDesign</skos:notation>
                             <skos:scopeNote xml:lang="en">
                             A mixed factorial design is an experimental design with two independent variables in which participants are randomly assigned to different levels of one independent variable and participate in all levels of the other independent variable. This design uses a combination of randomization and repeated measures to assign participants to treatment conditions. (source: http://www.psychwiki.com/wiki/What_is_a_mixed_design%3F)
                             </skos:scopeNote>
                             <ns0:status>candidate</ns0:status>
                             </skos:Concept>
                             </skos:hasTopConcept>
                             </skos:ConceptScheme>
                             </rdf:RDF>
                             """
                      )
      );
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

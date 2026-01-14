package eu.clarin.cmdi.curation.ccr;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.commons.conf.HttpConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.Times;
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

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

@SpringBootTest
@EnableConfigurationProperties
@EnableAutoConfiguration
@ComponentScan
@Import(CCRServiceTests.TestConfig.class)
@EnableCaching
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MockServerTest
class CCRServiceTests {


    private final CCRService service;

    @Autowired
    public CCRServiceTests(CCRService service) {
        this.service = service;
        // ensure all connection using HTTPS will use the SSL context defined by
        // MockServer to allow dynamically generated certificates to be accepted
//        HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(new MockServerLogger()).sslContext().getSocketFactory());
    }

    @Autowired
    private CacheManager cacheManager;
    @Autowired
    HttpConfig httpConfig;

    private MockServerClient mockServerClient;

    @MockServerPort
    private Integer mockServerPort;



    @Test
    void serviceNotAvailable() throws CCRServiceNotAvailableException {


        this.mockServerClient
                .when(
                        request()
                )
                .respond(
                        response().withStatusCode(200)
                );

        // we're making the proxy server inaccessible by incrementing the port to call
        httpConfig.setProxyPort(this.mockServerPort + 1);


        // should not throw any exception
        assertDoesNotThrow(() -> service.getConcept("http://hdl.handle.net/99999/CCR_C-4541_6dc2d021-1811-3d05-server-not-available"));
        // should return an unknown concept
        assertEquals("invalid concept", service.getConcept("http://hdl.handle.net/99999/CCR_C-4541_6dc2d021-1811-3d05-server-not-available").getPrefLabel());


        // resetting to the correct proxy server port
        httpConfig.setProxyPort(this.mockServerPort);
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
        assertDoesNotThrow(() -> service.getConcept("http://hdl.handle.net/99998/CCR_C-4541_6dc2d021-1811-3d05-56ca-timeout"));
        // should return an unknown concept
        assertEquals("invalid concept", service.getConcept("http://hdl.handle.net/99998/CCR_C-4541_6dc2d021-1811-3d05-56ca-timeout").getPrefLabel());
    }

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
        assertDoesNotThrow(() -> service.getConcept("http://hdl.handle.net/99997/CCR_C-4541_6dc2d021-1811-3d05-non-parseable"));
        // should return an unknown concept
        assertEquals("invalid concept", service.getConcept("http://hdl.handle.net/99997/CCR_C-4541_6dc2d021-1811-3d05-non-parseable").getPrefLabel());
    }

    @Test
    void useCache() throws CCRServiceNotAvailableException {
        this.mockServerClient
                .when(
                        request()
                )
                .respond(
                        response()
                                .withStatusCode(200)
                );

        service.getConcept("http://hdl.handle.net/11459/CCR_C-4541_6dc2d021-1811-3d05-use-cache");

        // getCache(...).get(...) returns only a value wrapper
        Assertions.assertNotNull(cacheManager.getCache("ccrCache").get("http://hdl.handle.net/11459/CCR_C-4541_6dc2d021-1811-3d05-use-cache"));
    }

    @Test
    void resultGroups() throws CCRServiceNotAvailableException {
                this.mockServerClient
                    .when(
                            request(),
                            Times.exactly(1)
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withBody("""
                                            <rdf:RDF>
                                            <skos:ConceptScheme rdf:about="http://hdl.handle.net/11459/CCR_P-Metadata_6f3f84d1-6f06-6291-4e20-4cd361cca128">
                                            <skos:hasTopConcept>
                                            <skos:Concept rdf:about="http://hdl.handle.net/11459/CCR_C-3796_e89bb008-3e2e-1f70-afa5-e506a6c12683">
                                            <ns0:status>candidate</ns0:status>
                                            <skos:inScheme rdf:resource="http://hdl.handle.net/11459/CCR_P-Metadata_6f3f84d1-6f06-6291-4e20-4cd361cca128"/>
                                            <skos:notation>fieldOfResearch</skos:notation>
                                            <skos:prefLabel xml:lang="en">field of research</skos:prefLabel>
                                            <skos:topConceptOf rdf:resource="http://hdl.handle.net/11459/CCR_P-Metadata_6f3f84d1-6f06-6291-4e20-4cd361cca128"/>
                                            <skos:altLabel>fieldOfResearch</skos:altLabel>
                                            <skos:altLabel xml:lang="en">research field</skos:altLabel>
                                            <skos:changeNote>
                                            This concept is based on the ISOcat data category: http://www.isocat.org/datcat/DC-3796
                                            </skos:changeNote>
                                            <skos:definition xml:lang="en">
                                            Indication of the linguistic field for assigning a resource type to its linguistic context. (source: NaLiDa)
                                            </skos:definition>
                                            <skos:example xml:lang="en">
                                            (general/applied) linguistics, phonetics, sociolinguistics, pragmatics, lexicography, etc.
                                            </skos:example>
                                            <skos:scopeNote xml:lang="en">
                                            Possible resource types: tool, corpus, lexicon, grammar, experiment, etc.
                                            </skos:scopeNote>
                                            </skos:Concept>
                                            </skos:hasTopConcept>
                                            <rdfs:label xml:lang="en">Metadata</rdfs:label>
                                            </skos:ConceptScheme>
                                            </rdf:RDF>
                                            """)
                    );

                this.mockServerClient
                    .when(
                            request(),
                            Times.exactly(1)
                    )
                    .respond(
                            response()
                                    .withStatusCode(200)
                                    .withBody("""
                                            <?xml version='1.0' encoding='UTF-8'?>
                                            <sparql xmlns='http://www.w3.org/2005/sparql-results#'>
                                            	<head>
                                            		<variable name='label'/>
                                            	</head>
                                            	<results>
                                            		<result>
                                            			<binding name='label'>
                                            				<literal xml:lang='en'>sound recording device</literal>
                                            			</binding>
                                            		</result>
                                            	</results>
                                            </sparql>
                                            """)
                    );

                final CCRConcept skosConcept = service.getConcept("http://hdl.handle.net/11459/CCR_C-3796_e89bb008-3e2e-1f70-afa5-e506a6c12683");

                assertAll(
                        "skos concept",
                        () -> assertEquals("http://hdl.handle.net/11459/CCR_C-3796_e89bb008-3e2e-1f70-afa5-e506a6c12683", skosConcept.getUri()),
                        () -> assertEquals("field of research", skosConcept.getPrefLabel()),
                        () -> assertEquals(CCRStatus.CANDIDATE, skosConcept.getStatus())
                );

                final CCRConcept wikidataConcept = service.getConcept("http://www.wikidata.org/entity/Q758891");

                assertAll(
                        "wikidata concept",
                        () -> assertEquals("http://www.wikidata.org/entity/Q758891", wikidataConcept.getUri()),
                        () -> assertEquals("sound recording device", wikidataConcept.getPrefLabel()),
                        () -> assertEquals(CCRStatus.APPROVED, wikidataConcept.getStatus())
                );



    }

    @SpringBootConfiguration
    @EnableCaching
    @ComponentScan(basePackages = "eu.clarin.cmdi.curation")
    public static class TestConfig {

    }
}

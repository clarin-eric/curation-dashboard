package eu.clarin.cmdi.curation.cr;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.commons.conf.HttpConfig;
import eu.clarin.cmdi.curation.cr.cache.CRCache;
import eu.clarin.cmdi.curation.cr.conf.CRConfig;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoCRCacheEntryException;
import eu.clarin.cmdi.curation.cr.exception.PPHCacheException;
import eu.clarin.cmdi.curation.cr.profile_parser.ParsedProfile;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockserver.client.MockServerClient;
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

import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


@SpringBootTest()
@Import(CRServiceTests.TestConfig.class)
@EnableConfigurationProperties
@EnableAutoConfiguration
@ComponentScan(basePackages = "eu.clarin.cmdi.curation")
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MockServerTest
class CRServiceTests {

    @Autowired
    CRCache crCache;
    @Autowired
    CRService crService;
    @Autowired
    CRConfig crConfig;
    @Autowired
    HttpConfig httpConfig;
    @Autowired
    CacheManager cacheManager;

    private MockServerClient mockServerClient;

    @MockServerPort
    private Integer mockServerPort;

    @Test
    void serverNotAvailable() {

        this.mockServerClient
                .when(
                        request()
                )
                .respond(
                        response().withStatusCode(200)
                );

        // we're making the proxy server inaccessible by incrementing the port to call
        httpConfig.setProxyPort(this.mockServerPort + 1);

        // we are using a proxy server which responds with ok status, but since we call it on the wrong port, the server is not accessible
        // no exception should be thrown in crCache but a null value should be cached for the profile
        assertDoesNotThrow(() -> this.crCache.getEntry("http://www.wowasa.com/clarin.eu:cr1:p_1403526079381/xsd"));
        // the attempt to get the parsed profile should result in an exception, since we can't create a ParsedProfile from null
        assertThrows(NoCRCacheEntryException.class, () -> crService.getParsedProfile("http://www.wowasa.com/clarin.eu:cr1:p_1403526079381/xsd"));
        // there should by a cache entry (we're testing for the existence of a cache entry, not for its value!)
        assertNotNull(cacheManager.getCache("crCache").get("http://www.wowasa.com/clarin.eu:cr1:p_1403526079381/xsd"));
        // even with a non-existent local file it shouldn't throw an exception
        assertDoesNotThrow(() -> this.crCache.getEntry(Paths.get("tmp", "77777").toUri().toString()));

        // resetting to the correct proxy server port
        httpConfig.setProxyPort(this.mockServerPort);
    }

    @Test
    void connectionTimeout() throws NoCRCacheEntryException, CRServiceStorageException, CCRServiceNotAvailableException, PPHCacheException {

        this.mockServerClient
                .when(
                        request()
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withDelay(TimeUnit.SECONDS, 6)
                );

        // we are using a proxy server which responds with ok status after 6 seconds, which is more than the standard connection timeout of 5 seconds
        // no exception should be thrown in crCache but a null value should be cached for the profile
        assertDoesNotThrow(() -> this.crCache.getEntry("http://www.wowasa.com/clarin.eu:cr1:p_1403526079382/xsd"));
        // the attempt to get the parsed profile should result in an exception, since we can't create a ParsedProfile from null
        assertThrows(NoCRCacheEntryException.class, () -> crService.getParsedProfile("http://www.wowasa.com/clarin.eu:cr1:p_1403526079382/xsd"));
        // there should by a cache entry (we're testing for the existence of a cache entry, not for its value!)
        assertNotNull(cacheManager.getCache("crCache").get("http://www.wowasa.com/clarin.eu:cr1:p_1403526079382/xsd"));
    }

    @Test
    void nonParseableResult() throws CCRServiceNotAvailableException, CRServiceStorageException, PPHCacheException, NoCRCacheEntryException {

        this.mockServerClient
                .when(
                        request()
                )
                .respond(
                        response()
                                .withBody("")
                );

        assertDoesNotThrow(() -> this.crCache.getEntry("http://www.wowasa.com/clarin.eu:cr1:p_1403526079382/xsd"));
        assertThrows(NoCRCacheEntryException.class, () -> this.crService.getParsedProfile("http://www.wowasa.com/clarin.eu:cr1:p_1403526079382/xsd"));

        assertNull(this.crCache.getEntry("http://www.wowasa.com/clarin.eu:cr1:p_1403526079382/xsd"));


        String schemaLocation = "http://www.wowasa.com/" + new Random().nextInt();

        assertNull(this.crCache.getEntry(schemaLocation));

        assertFalse(this.crService.isPublicSchema(schemaLocation));
        // should be cached
        assertNotNull(cacheManager.getCache("crCache").get(schemaLocation));

    }

    @Test
    void isPublic() {

        assertDoesNotThrow(() -> crService.getParsedProfile("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1380106710826/xsd"));

        ParsedProfile parsedProfile = null;

        try {

            parsedProfile = crService.getParsedProfile("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1380106710826/xsd");
        }
        catch (Exception e) {

            log.error("error in schema");
            log.error("", e);
        }
        // the profile is in the context registry
        assertTrue(parsedProfile.header().isCrResident());
        // and it is public
        assertTrue(parsedProfile.header().isPublic());

        try {

            parsedProfile = crService.getParsedProfile("file:///tmp/17661427897514518579.tmp");
        }
        catch (Exception e) {

            log.error("error in schema");
            log.error("", e);
        }
        // uploaded profiles are not in the context registry
        assertFalse(parsedProfile.header().isCrResident());
        // uploaded profiles can't be public, since only profiles from the context registry can be public
        assertFalse(parsedProfile.header().isPublic());
    }

    @SpringBootConfiguration
    @EnableCaching
    public static class TestConfig {

    }
}

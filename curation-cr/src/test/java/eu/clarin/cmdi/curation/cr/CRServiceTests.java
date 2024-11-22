package eu.clarin.cmdi.curation.cr;

import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.commons.conf.HttpConfig;
import eu.clarin.cmdi.curation.cr.cache.CRCache;
import eu.clarin.cmdi.curation.cr.conf.CRConfig;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoCRCacheEntryException;
import eu.clarin.cmdi.curation.cr.exception.PPHCacheException;
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

    //@Test
    void serverNotAvailable() {

        this.mockServerClient
                .when(
                        request()
                )
                .respond(
                        response().withStatusCode(200)
                );

        httpConfig.setProxyPort(this.mockServerPort + 1);

        try {
            assertDoesNotThrow(() -> this.crCache.getEntry("http://www.wowasa.com/clarin.eu:cr1:p_1403526079381/xsd"));
            assertThrows(NoCRCacheEntryException.class, () -> crService.getParsedProfile("http://www.wowasa.com/clarin.eu:cr1:p_1403526079381/xsd"));
            // should be cached
            assertNotNull(cacheManager.getCache("crCache").get("http://www.wowasa.com/clarin.eu:cr1:p_1403526079381/xsd"));
            // even with a local file it should throw an exception
            assertDoesNotThrow(() -> this.crCache.getEntry(Paths.get("tmp", "77777").toUri().toString()));
        }
        catch (Exception e) {

        }

        httpConfig.setProxyPort(this.mockServerPort);
    }

    //@Test
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

        assertDoesNotThrow(() -> this.crCache.getEntry("http://www.wowasa.com/clarin.eu:cr1:p_1403526079382/xsd"));
        assertThrows(NoCRCacheEntryException.class, () -> this.crService.getParsedProfile("http://www.wowasa.com/clarin.eu:cr1:p_1403526079382/xsd"));

        assertFalse(this.crService.isPublicSchema("http://www.wowasa.com/clarin.eu:cr1:p_1403526079382/xsd"));
        // should be cached
        assertNotNull(cacheManager.getCache("crCache").get("http://www.wowasa.com/clarin.eu:cr1:p_1403526079382/xsd"));


    }

    //@Test
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

        try {
            assertDoesNotThrow(() -> crService.getParsedProfile("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1403526079380/xsd"));
            assertTrue(crService.getParsedProfile("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1403526079380/xsd").header().isPublic());
//            assertDoesNotThrow(() -> crService.getParsedProfile("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_9990106710826/xsd"));
//            assertFalse(crService.getParsedProfile("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_9990106710826/xsd").header().isPublic());
            assertFalse(crService.getParsedProfile(this.crConfig.getCrCache().resolve("https___catalog_clarin_eu_ds_ComponentRegistry_rest_registry_1_x_profiles_clarin_eu_cr1_p_1403526079380_xsd.xsd").toUri().toString()).header().isPublic());
        }
        catch (Exception e) {

            log.error("error in schema");
            log.error("", e);
        }
    }

    @Test
    void cacheUsage() {

        try {

            crService.getParsedProfile("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1403526079380/xsd");
 //           crService.getParsedProfile("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_9990106710826/xsd");

            crService.getParsedProfile(this.crConfig.getCrCache().resolve("https___catalog_clarin_eu_ds_ComponentRegistry_rest_registry_1_x_profiles_clarin_eu_cr1_p_1403526079380_xsd.xsd").toUri().toString());

            assertNotNull(cacheManager.getCache("crCache").get("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1403526079380/xsd"));
//            assertNotNull(cacheManager.getCache("crCache").get("https://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_9990106710826/xsd"));
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

package eu.clarin.cmdi.curation.web.controller;

import eu.clarin.cmdi.curation.web.conf.WebConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockserver.client.MockServerClient;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.socket.tls.KeyStoreFactory;
import org.mockserver.springtest.MockServerTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;


@MockServerTest
public class BaseCtlTest {


    protected final MockMvc mockMvc;

    protected final WebConfig webConfig;

    private MockServerClient mockServerClient;


    public BaseCtlTest(MockMvc mockMvc, WebConfig webConfig) {
        this.mockMvc = mockMvc;
        this.webConfig = webConfig;
    }

    @BeforeAll
    public static void init() throws IOException {
        // ensure all connection using HTTPS will use the SSL context defined by
        // MockServer to allow dynamically generated certificates to be accepted
        HttpsURLConnection.setDefaultSSLSocketFactory(new KeyStoreFactory(new MockServerLogger()).sslContext().getSocketFactory());
    }
    @BeforeEach
    public void createExpectations() throws URISyntaxException, IOException {
        this.mockServerClient
                .when(
                        request()
                                .withPath("/ds/ComponentRegistry/rest/registry/1.x/profiles")
                )
                .respond(
                        response()
                                .withBody("""
                                        <profileDescriptions>
                                        <profileDescription>
                                            <id>clarin.eu:cr1:p_1380106710826</id>
                                            <name>teiHeader</name>
                                            <description>
                                            TEI header. Supplies the descriptive and declarative information making up an electronic title page prefixed to every TEI-conformant text. Version 2.5.0. Last updated on 26th July 2013 http://www.tei-c.org/release/doc/tei-p5-doc/en/html/ref-teiHeader.html
                                            </description>
                                            <status>PRODUCTION</status>
                                        </profileDescription>
                                        </profileDescriptions>
                                        """)
                );
        this.mockServerClient
                .when(
                        request()
                                .withPath("/ds/ComponentRegistry/rest/registry/1.x/profiles/clarin.eu:cr1:p_1380106710826/xsd")
                )
                .respond(
                        response()
                                .withBody(Files.readString(Paths.get(getClass().getResource("/testfiles/teiHeader.xsd").toURI())))
                );
        this.mockServerClient
                .when(
                        request()
                )
                .respond(
                        response()
                                .withStatusCode(200)
                );
    }
}

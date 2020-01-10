package eu.clarin.cmdi.curation.ccr_service;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

class CCRService implements ICCRService {

    private static Logger _logger = LoggerFactory.getLogger(CCRService.class);

    static final String CCR_SINGLE_CONCEPT = CCRServiceFactory.CCR_REST_API_URL
            + "concept?fl=status,%20prefLabel@en&format=json&id=";
    static final String CCR_ALL_CONCEPTS = CCRServiceFactory.CCR_REST_API_URL
            + "find-concepts?q=status:[*%20TO%20*]&fl=status,uri,prefLabel@en&format=json&rows=5000";

    private volatile Map<String, CCRConcept> cache;

    CCRService() {
        cache = new HashMap<>();
        try {
            init();
        } catch (Exception e) {
            _logger.error("Unable to initialize CCRService. Probably CCR is not available at the moment", e);

            // wowasa (2017-05-09): whole application crashes in case the error
            // is thrown here
            // throw new RuntimeException("Unable to initialize CCRService.
            // Probably CCR is not available at the moment", e);
        }
    }

    ;

    @Override
    public CCRConcept getConcept(String url) {
        return cache.get(url);
    }

    @Override
    public Collection<CCRConcept> getAll() {
        return cache.values();
    }

    private void init() throws IOException, NoSuchAlgorithmException, KeyManagementException {
        _logger.debug("Fetching from {}", CCR_ALL_CONCEPTS);

        /* wowasa (2017-05-26): validation check might be switched off to bypass expired certificates.
         * System-property can be set with the following entry in web.xml
         * <env-entry>
         * <env-entry-name>crrservice.ssl.validate</env-entry-name>
         * <env-entry-type>java.lang.String</env-entry-type>
         * <env-entry-value>off</env-entry-value>
         * </env-entry>
         */

        if (System.getProperty("crrservice.ssl.validate", "on").equalsIgnoreCase("off")) {
            _logger.warn("SSL-certificate check in CRRService deaktivated");

            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        } // end switch off validation check

        HttpsURLConnection
                .setDefaultHostnameVerifier((hostname, session) -> hostname.equals("openskos.meertens.knaw.nl"));

        String json = IOUtils.toString(new URL(CCR_ALL_CONCEPTS).openStream(), StandardCharsets.UTF_8);
        JsonObject obj = new Gson().fromJson(json, JsonObject.class);
        obj = obj.getAsJsonObject("response");
        JsonElement numberOfRecords = obj.get("numFound");
        int numFound = numberOfRecords.getAsInt();

        _logger.debug("Number of found concepts in CCR is {}", numFound);

        JsonArray conceptsArray = obj.getAsJsonArray("docs");
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(CCRConcept.class, new ConceptTypeAdapter());
        final Gson gson = gsonBuilder.create();
        conceptsArray.forEach(c -> {
            JsonObject conceptJson = c.getAsJsonObject();
            CCRConcept concept = gson.fromJson(conceptJson, CCRConcept.class);
            cache.put(concept.uri, concept);

        });
    }
}

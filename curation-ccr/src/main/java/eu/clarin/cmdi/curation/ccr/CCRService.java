package eu.clarin.cmdi.curation.ccr;

import com.google.gson.*;

import eu.clarin.cmdi.curation.ccr.config.CCRProperties;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;

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

@Slf4j
class CCRService implements ICCRService {

    private volatile Map<String, CCRConcept> cache;


    
    CCRService(CCRProperties ccrProps) {
        cache = new HashMap<>();
        try {
            init(ccrProps);
        } catch (Exception e) {
            log.error("Unable to initialize CCRService. Probably CCR is not available at the moment", e);

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

    private void init(CCRProperties ccrProps) throws IOException, NoSuchAlgorithmException, KeyManagementException {
        log.debug("Fetching from {}", ccrProps.getRestApiUrl());

        /* wowasa (2017-05-26): validation check might be switched off to bypass expired certificates.
         * System-property can be set with the following entry in web.xml
         * <env-entry>
         * <env-entry-name>crrservice.ssl.validate</env-entry-name>
         * <env-entry-type>java.lang.String</env-entry-type>
         * <env-entry-value>off</env-entry-value>
         * </env-entry>
         */

        if (System.getProperty("crrservice.ssl.validate", "on").equalsIgnoreCase("off")) {
            log.warn("SSL-certificate check in CRRService deaktivated");

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

        String json = IOUtils.toString(new URL(ccrProps.getRestApiUrl() + ccrProps.getAllConceptsQuery()).openStream(), StandardCharsets.UTF_8);
        JsonObject obj = new Gson().fromJson(json, JsonObject.class);
        obj = obj.getAsJsonObject("response");
        JsonElement numberOfRecords = obj.get("numFound");
        int numFound = numberOfRecords.getAsInt();

        log.debug("Number of found concepts in CCR is {}", numFound);

        JsonArray conceptsArray = obj.getAsJsonArray("docs");
        final GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(CCRConcept.class, new ConceptTypeAdapter());
        final Gson gson = gsonBuilder.create();
        conceptsArray.forEach(c -> {
            JsonObject conceptJson = c.getAsJsonObject();
            CCRConcept concept = gson.fromJson(conceptJson, CCRConcept.class);
            cache.put(concept.getUri(), concept);

        });
    }
}

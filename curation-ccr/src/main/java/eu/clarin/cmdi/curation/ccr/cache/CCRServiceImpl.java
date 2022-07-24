package eu.clarin.cmdi.curation.ccr.cache;

import com.google.gson.*;

import eu.clarin.cmdi.curation.ccr.CCRConcept;
import eu.clarin.cmdi.curation.ccr.CCRService;
import eu.clarin.cmdi.curation.ccr.ConceptTypeAdapter;
import eu.clarin.cmdi.curation.ccr.config.CCRProperties;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
class CCRServiceImpl implements CCRService {
   
   private final CCRProperties ccrProps;
    
    @Autowired
    public CCRServiceImpl(CCRProperties ccrProps) {
       this.ccrProps = ccrProps;
    }

    @Override
    public CCRConcept getConcept(String url) {
        return getCCRConceptMap().get(url);
    }

    @Override
    public Collection<CCRConcept> getAll() {
        return getCCRConceptMap().values();
    }

    
    @Cacheable("ccrService")
    private Map<String, CCRConcept> getCCRConceptMap() {
       final Map<String, CCRConcept> concepts = new HashMap<String, CCRConcept>();
       
       log.debug("Fetching from {}", ccrProps.getRestApiUrl());

       /* wowasa (2017-05-26): validation check might be switched off to bypass expired certificates.
        * System-property can be set with the following entry in web.xml
        * <env-entry>
        * <env-entry-name>crrservice.ssl.validate</env-entry-name>
        * <env-entry-type>java.lang.String</env-entry-type>
        * <env-entry-value>off</env-entry-value>
        * </env-entry>
        */

       try {
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
              concepts.put(concept.getUri(), concept);

          });
      }
      catch (Exception e) {
         
         log.error("Unable to initialize CCRService. Probably CCR is not available at the moment", e);
      }
 
       
       return concepts;
    }
}

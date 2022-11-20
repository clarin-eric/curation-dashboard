package eu.clarin.cmdi.curation.ccr.cache;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import eu.clarin.cmdi.curation.ccr.CCRConcept;
import eu.clarin.cmdi.curation.ccr.ConceptTypeAdapter;
import eu.clarin.cmdi.curation.ccr.conf.CCRConfig;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CCRCache {
   
   @Autowired
   private CCRConfig ccrProps;
   
   @Cacheable(value = "ccrCache", key = "#root.methodName")
   public Map<String, CCRConcept> getCCRConceptMap() {
      
      final Map<String, CCRConcept>concepts = new ConcurrentHashMap<String, CCRConcept>();

      log.debug("Fetching from {}", ccrProps.getRestApi());

      /*
       * wowasa (2017-05-26): validation check might be switched off to bypass expired
       * certificates. System-property can be set with the following entry in web.xml
       * <env-entry> <env-entry-name>crrservice.ssl.validate</env-entry-name>
       * <env-entry-type>java.lang.String</env-entry-type>
       * <env-entry-value>off</env-entry-value> </env-entry>
       */

      if (System.getProperty("ccrservice.ssl.validate", "on").equalsIgnoreCase("off")) {
         try {
            log.warn("SSL-certificate check in CCRService deaktivated");

            TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
               public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                  return null;
               }

               public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
               }

               public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
               }
            } };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            
            String refHostName = new URL(ccrProps.getRestApi()).getHost();
            
            HttpsURLConnection
            .setDefaultHostnameVerifier((hostname, session) -> hostname.equals(refHostName));


         }
         catch (NoSuchAlgorithmException ex) {

            log.error("SSL algorithm not available from SSL context");
            throw new CCRServiceNotAvailableException(ex);

         }
         catch (KeyManagementException ex) {
            
            log.error("couldn't set trust all certificate - this might be forbidden by policy settings");
            throw new CCRServiceNotAvailableException(ex);
            
         }
         catch (MalformedURLException ex) {
            
            log.error("can't extract hostname from URL '{}'", ccrProps.getRestApi());
            throw new CCRServiceNotAvailableException(ex);            
         }
      } // end switch off validation check
      


      
      String restApiUrl = ccrProps.getRestApi() + ccrProps.getQuery();
      
      try {
         String json = IOUtils.toString(new URL(restApiUrl).openStream(),
               StandardCharsets.UTF_8);
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
      catch(MalformedURLException ex) {
         
         log.error("the URL '{}' is no valid URL for lookup", restApiUrl);
         throw new CCRServiceNotAvailableException(ex);
         
      }
      catch (IOException ex) {
         
         log.error("can't parse incoming stream to JSON object", ex);
         throw new CCRServiceNotAvailableException(ex);
      }
      
      return concepts;
   }

}

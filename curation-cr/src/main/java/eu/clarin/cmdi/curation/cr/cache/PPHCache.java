package eu.clarin.cmdi.curation.cr.cache;

import eu.clarin.cmdi.curation.commons.http.HttpUtils;
import eu.clarin.cmdi.curation.cr.conf.CRConfig;
import eu.clarin.cmdi.curation.cr.exception.PPHCacheException;
import eu.clarin.cmdi.curation.cr.profile_parser.ProfileHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Pph cache.
 */
@Component
@Slf4j
public class PPHCache {

   @Autowired
   private final HttpUtils httpUtils;
   @Autowired
   CRConfig crConfig;

    public PPHCache(HttpUtils httpUtils) {
        this.httpUtils = httpUtils;
    }

    /**
    * Gets profile headers map.
    *
    * @return the profile headers map with profile id as key
    */
   @Cacheable(value = "pphCache", sync = true)
   public Map<String, ProfileHeader> getProfileHeadersMap() throws PPHCacheException {

      String fileName = crConfig.getRestApi().replaceAll("[/.:]", "_");

      Path xml = crConfig.getCrCache().resolve(fileName + ".xml");

      // if not download it

      try {
         if (Files.notExists(xml) || Files.size(xml) == 0) {

            if(Files.notExists(crConfig.getCrCache())) {

               try {

                  Files.createDirectories(crConfig.getCrCache());
               }
               catch (IOException e) {

                  log.error("could create cr cache directory '{}'", crConfig.getCrCache());
               }
            }

            log.debug("XML is not in the local cache, it will be downloaded");

            try(InputStream in = httpUtils.getURLConnection(crConfig.getRestApi() + crConfig.getQuery()).getInputStream()) {

               Files.copy(in, xml, StandardCopyOption.REPLACE_EXISTING);
            }
         }
      }
      catch (MalformedURLException e) {

         log.error("the concept registry URL '{}' is malformed", crConfig.getRestApi() + crConfig.getQuery());
         throw new PPHCacheException(e);
      }
      catch (IOException e) {

         log.error("can't access xml file '{}'", xml);
         throw new PPHCacheException(e);
      }

      Map<String, ProfileHeader> profileHeaders = new ConcurrentHashMap<>();

      try (InputStream in = Files.newInputStream(xml)){

         SAXParserFactory fac = SAXParserFactory.newInstance();
         SAXParser parser = fac.newSAXParser();

         log.trace("component registry URL: {}", (crConfig.getRestApi() + crConfig.getQuery()));

         parser.parse(in, new DefaultHandler() {

            private StringBuilder elementValue;
            private String id, name, description, status;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes)
                  throws SAXException {

               switch(qName) {
                  case "profileDescription" -> {
                     this.id = null;
                     this.name = null;
                     this.description = null;
                  }
                  case "id", "name", "description", "status" -> elementValue = new StringBuilder();
               }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {

               switch (qName) {
                  case "profileDescription" -> profileHeaders.put((crConfig.getRestApi() + "/" + this.id + "/xsd"),
                                       new ProfileHeader(
                                   "1.x",
                                               (crConfig.getRestApi() + "/" + this.id + "/xsd"),
                                               this.id,
                                               this.name,
                                               this.description,
                                               this.status,
                                       true
                                       )
                  );
                  case "id" -> this.id = elementValue.toString();
                  case "name" -> this.name = elementValue.toString();
                  case "description" -> this.description = elementValue.toString();
                  case "status" -> this.status = elementValue.toString().toLowerCase();
               }
            }

            @Override
            public void characters(char[] ch, int start, int length) throws SAXException {
               if (elementValue == null) {
                  elementValue = new StringBuilder();
               }
               else {
                  elementValue.append(ch, start, length);
               }
            }
         });
      }

      catch (IOException e) {
         
         log.error("IOException while reading stream from file '{}'", xml);
         throw new PPHCacheException(e);
      }
      catch (SAXException e) {

         log.error("stream not parsable for ProfileHeader");
         throw new PPHCacheException(e);
      }
      catch (ParserConfigurationException e) {

         log.error("can't configure new SAXParser");
         throw new PPHCacheException(e);
      }

      return profileHeaders;
   }
}
package eu.clarin.cmdi.curation.pph.cache;

import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.curation.pph.exception.PPHServiceNotAvailableException;
import lombok.extern.slf4j.Slf4j;
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
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The type Pph cache.
 */
@Component
@Slf4j
public class PPHCache {

   /**
    * Gets profile headers map.
    *
    * @param restUrl the rest url
    * @param query   the query part added to the rest url
    * @return the profile headers map with profile id as key
    */
   @Cacheable(value = "pphCache", key = "#query")
   public Map<String, ProfileHeader> getProfileHeadersMap(String restUrl, String query) throws PPHServiceNotAvailableException {

      Map<String, ProfileHeader> profileHeaders = new ConcurrentHashMap<String, ProfileHeader>();

      try {

         SAXParserFactory fac = SAXParserFactory.newInstance();
         SAXParser parser = fac.newSAXParser();

         log.trace("component registry URL: {}", (restUrl + query));

         InputStream in = new URL(restUrl + query).openStream();

         parser.parse(in, new DefaultHandler() {

            private StringBuilder elementValue;
            private ProfileHeader header;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes)
                  throws SAXException {

               switch(qName) {
                  case "profileDescription" -> {
                     header = new ProfileHeader();
                     header.setCmdiVersion("1.x");
                     header.setPublic(true);
                  }
                  case "id", "name", "description", "status" -> elementValue = new StringBuilder();
               }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {

               switch (qName) {
                  case "id" -> {
                     header.setId(elementValue.toString());
                     profileHeaders.put(header.getId(), header);
                     header.setSchemaLocation(restUrl + "/" + elementValue.toString() + "/xsd");
                  }
                  case "name" -> header.setName(elementValue.toString());
                  case "description" -> header.setDescription(elementValue.toString());
                  case "status" -> header.setStatus(elementValue.toString().toLowerCase());
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

      catch (MalformedURLException e) {

         log.error("malformed lookup URL '{}'", (restUrl + query));
         throw new PPHServiceNotAvailableException(e);

      }
      catch (IOException e) {
         
         log.error("IOException while reading stream from URL '{}'", (restUrl + query));
         throw new PPHServiceNotAvailableException(e);
         
      }
      catch (SAXException e) {

         log.error("stream not parsable for ProfileHeader");
         throw new PPHServiceNotAvailableException(e);

      }
      catch (ParserConfigurationException e) {

         log.error("can't configure new SAXParser");
         throw new PPHServiceNotAvailableException(e);
      }

      return profileHeaders;
   }
}

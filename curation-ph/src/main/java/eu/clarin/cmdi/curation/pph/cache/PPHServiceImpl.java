package eu.clarin.cmdi.curation.pph.cache;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import eu.clarin.cmdi.curation.pph.ProfileHeader;
import eu.clarin.cmdi.curation.pph.PPHService;
import eu.clarin.cmdi.curation.pph.conf.PHProperties;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PPHServiceImpl implements PPHService {

   @Autowired
   PHProperties props;

   @Cacheable("profileHeaderCache")
   public Collection<ProfileHeader> getProfileHeaders(String restUrl, String query) {

      Vector<ProfileHeader> profileHeaders = new Vector<ProfileHeader>();

      try {

         SAXParserFactory fac = SAXParserFactory.newInstance();
         SAXParser parser = fac.newSAXParser();

         log.trace("component registry URL: {}", (restUrl + query));

         InputStream in = new URL(restUrl + query).openStream();

         parser.parse(in, new DefaultHandler() {

            private StringBuilder elementValue;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes)
                  throws SAXException {
               switch(qName) {
                  case "profileDescription":
                     profileHeaders.add(new ProfileHeader());
                     profileHeaders.lastElement().setCmdiVersion("1.x");
                     profileHeaders.lastElement().setPublic(true);
                     break;
                     
                  case "id":
                  case "name":
                  case "description":
                  case "status":
                     elementValue = new StringBuilder();
                     break;
                  default:
                     break;
               }
            }

            @Override
            public void endElement(String uri, String localName, String qName) throws SAXException {
               switch (qName) {
                  case "id":
                     profileHeaders.lastElement().setId(elementValue.toString());
                     profileHeaders.lastElement().setSchemaLocation(restUrl + "/" + elementValue.toString() + "/xsd");
                     break;
                  case "name":
                     profileHeaders.lastElement().setName(elementValue.toString());
                     break;
                  case "description":
                     profileHeaders.lastElement().setDescription(elementValue.toString());
                     break;
                  case "status":
                     profileHeaders.lastElement().setStatus(elementValue.toString().toLowerCase());
                     break;
                   default:
                      break;
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

      }
      catch (IOException e) {
         
         log.error("IOException while reading stream from URL '{}'", (restUrl + query));
         
      }
      catch (SAXException e) {

         log.error("stream not parsable for ProfileHeader");

      }
      catch (ParserConfigurationException e) {

         log.error("", e);

      }

      return profileHeaders;
   }

   @Override
   public Collection<ProfileHeader> getProfileHeaders() {

      return getProfileHeaders(props.getCrRestUrl(), props.getCrQuery());

   }
}

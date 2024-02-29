/**
 * @author Wolfgang Walter SAUER (wowasa) &lt;clarin@wowasa.com&gt;
 *
 */
package eu.clarin.cmdi.curation.api.xml;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * The type X path value service.
 */
@Service
@Slf4j
public class XPathValueService {

   /**
    * Get xpath value map map.
    *
    * @param xmlFilePath the xml file path
    * @return the map
    */
   public Map<String, String> getXpathValueMap(Path xmlFilePath){
      
      Map<String, String> xpathValueMap = new LinkedHashMap<String, String>();
      
      try {
         SAXParserFactory fac = SAXParserFactory.newInstance(); 
         
         SAXParser parser = fac.newSAXParser();
         
         try(InputStream in = Files.newInputStream(xmlFilePath)){
            parser.parse(Files.newInputStream(xmlFilePath), new ServiceHandler(xpathValueMap));
         }
         catch (IOException e) {
            
            log.error("can't read file {}", xmlFilePath);
         }
         catch (SAXException e) {
            
            log.error("can't SAX parse file {}", xmlFilePath);
         }
      }
      catch (ParserConfigurationException | SAXException e) {

         log.error("can't create SAXPArser");
      }
      
      return xpathValueMap;
   }
   
   private class ServiceHandler extends DefaultHandler {
      // to add a counter to the element path, if we have multiple elements of the same path
      // sample: /element[2]/text()
      private final Map<Path, Integer> elementCount = new HashMap<Path, Integer>();
      
      private final Map<String, String> xpathValueMap;      
      
      private Path xpath = Paths.get("/");
      
      private StringBuilder elementValue;

      /**
       * Instantiates a new Service handler.
       *
       * @param xpathValueMap the xpath value map
       */
      public ServiceHandler(Map<String, String> xpathValueMap) {
         
         this.xpathValueMap = xpathValueMap;
      }


      /**
       * Start element.
       *
       * @param uri        the uri
       * @param localName  the local name
       * @param qName      the q name
       * @param attributes the attributes
       * @throws SAXException the sax exception
       */
      @Override
      public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
         
         this.xpath = xpath.resolve(qName);
         
         IntStream.range(0, attributes.getLength())
            .filter(i -> StringUtils.isNoneBlank(attributes.getValue(i)))
            .forEach(i ->  xpathValueMap.put(this.xpath.resolve("@" + attributes.getQName(i)).toString(), attributes.getValue(i)));
      }

      /**
       * End element.
       *
       * @param uri       the uri
       * @param localName the local name
       * @param qName     the q name
       * @throws SAXException the sax exception
       */
      @Override
      public void endElement(String uri, String localName, String qName) throws SAXException {
         
         if(this.elementValue != null && StringUtils.isNotBlank(this.elementValue.toString())) {
            
            if(this.elementCount.containsKey(this.xpath)) {
               
               this.xpathValueMap.put(this.xpath.getParent().resolve(xpath.getFileName() + "[" + this.elementCount.compute(xpath, (k, v) -> ++v) + "]") .resolve("text()").toString(), this.elementValue.toString().trim());
            }
            else {
               
               this.elementCount.put(xpath, 1);
               this.xpathValueMap.put(this.xpath.resolve("text()").toString(), this.elementValue.toString().trim());
            }               
            
            this.elementValue = null;
         }
         
         this.xpath = this.xpath.getParent();
      }

      /**
       * Characters.
       *
       * @param ch     the ch
       * @param start  the start
       * @param length the length
       * @throws SAXException the sax exception
       */
      @Override
      public void characters(char[] ch, int start, int length) throws SAXException {
         
        if (elementValue == null) {
            elementValue = new StringBuilder();
        } 
        else {
            elementValue.append(ch, start, length);
        }
      }      
   }

}

package eu.clarin.helpers;

import java.util.Iterator;
import java.util.stream.Stream;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import eu.clarin.cmdi.curation.utils.TimeUtils;
import eu.clarin.cmdi.rasa.DAO.CheckedLink;
import lombok.extern.slf4j.Slf4j;

/**
 * @author WolfgangWalter SAUER (wowasa)
 *
 */
@Slf4j
public class CheckedLinkStAXReader implements XMLStreamReader {
   
   private static final String ROOT_ELEMENT_NAME = "checkedLinks";
   private static final String ELEMENT_NAME = "checkedLink";
   
   private static final int[] BASIC_STRUCTURE = {
         XMLStreamConstants.START_DOCUMENT,
         XMLStreamConstants.START_ELEMENT,
         XMLStreamConstants.START_ELEMENT,
         XMLStreamConstants.END_ELEMENT,
         XMLStreamConstants.END_ELEMENT,
         XMLStreamConstants.END_DOCUMENT
   };
   
   private static final QName[] ATTRIBUTE_NAME = {
         new QName("url"),
         new QName("method"),
         new QName("statusCode"),
         new QName("category"),
         new QName("byteSize"),
         new QName("duration"),
         new QName("checkingDate")
   };
   

   private Stream<CheckedLink> checkedLinkStream;
   private Iterator<CheckedLink> checkedLinkIter;
   private CheckedLink checkedLink;
   
   private int basicIndex;
   
   
   public CheckedLinkStAXReader(Stream<CheckedLink> checkedLinkStream) {
      
      this.checkedLinkStream = checkedLinkStream;
      this.checkedLinkIter = checkedLinkStream.iterator();      

   }

   @Override
   public void close() throws XMLStreamException {
      
      
   }

   @Override
   public int getAttributeCount() {

      return (this.basicIndex == 2?7:0);
   }

   @Override
   public String getAttributeLocalName(int index) {
      
      return ATTRIBUTE_NAME[index].getLocalPart();
   }

   @Override
   public QName getAttributeName(int index) {
      
      return ATTRIBUTE_NAME[index];
   }

   @Override
   public String getAttributeNamespace(int arg0) {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public String getAttributePrefix(int arg0) {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public String getAttributeType(int arg0) {
      
      return "CDATA";
   }

   @Override
   public String getAttributeValue(int index) {
      switch(index){
      case 0:
         return this.checkedLink.getUrl();
      case 1:
         return this.checkedLink.getMethod();
      case 2:
         return (this.checkedLink.getStatus() != null?this.checkedLink.getStatus().toString():"n/a");
      case 3:
         return this.checkedLink.getCategory().name();
      case 4:
         return (this.checkedLink.getByteSize() != null?this.checkedLink.getByteSize().toString():"n/a");
      case 5:
         return (this.checkedLink.getDuration() != null? this.checkedLink.getDuration().toString():"n/a");
      case 6:
         return TimeUtils.humanizeToDate(this.checkedLink.getCheckingDate().getTime());
      }
      return null;
   }

   @Override
   public String getAttributeValue(String arg0, String arg1) {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public String getCharacterEncodingScheme() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public String getElementText() throws XMLStreamException {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public String getEncoding() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public int getEventType() {

      return BASIC_STRUCTURE[this.basicIndex];
   
   }

   @Override
   public String getLocalName() {
      
      return getName().getLocalPart();
   }

   @Override
   public Location getLocation() {
      return new Location() {

         @Override
         public int getCharacterOffset() {
            log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
            return 0;
         }

         @Override
         public int getColumnNumber() {
            log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
            return 0;
         }

         @Override
         public int getLineNumber() {
            log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
            return 0;
         }

         @Override
         public String getPublicId() {
            log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
            return null;
         }

         @Override
         public String getSystemId() {
            log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
            return null;
         }
         
      };
   }

   @Override
   public QName getName() {
      System.out.println(new Object() {}.getClass().getEnclosingMethod().getName());
      
      return new QName(this.checkedLink == null?ROOT_ELEMENT_NAME:ELEMENT_NAME);
   
   }

   @Override
   public NamespaceContext getNamespaceContext() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public int getNamespaceCount() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return 0;
   }

   @Override
   public String getNamespacePrefix(int arg0) {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public String getNamespaceURI() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public String getNamespaceURI(String arg0) {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public String getNamespaceURI(int arg0) {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public String getPIData() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public String getPITarget() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public String getPrefix() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public Object getProperty(String arg0) throws IllegalArgumentException {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public String getText() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public char[] getTextCharacters() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public int getTextCharacters(int arg0, char[] arg1, int arg2, int arg3) throws XMLStreamException {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return 0;
   }

   @Override
   public int getTextLength() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return 0;
   }

   @Override
   public int getTextStart() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return 0;
   }

   @Override
   public String getVersion() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return null;
   }

   @Override
   public boolean hasName() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return false;
   }

   @Override
   public boolean hasNext() throws XMLStreamException {

      return this.basicIndex <=6;
   }

   @Override
   public boolean hasText() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return false;
   }

   @Override
   public boolean isAttributeSpecified(int arg0) {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return false;
   }

   @Override
   public boolean isCharacters() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return false;
   }

   @Override
   public boolean isEndElement() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return false;
   }

   @Override
   public boolean isStandalone() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return false;
   }

   @Override
   public boolean isStartElement() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return false;
   }

   @Override
   public boolean isWhiteSpace() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return false;
   }

   @Override
   public int next() throws XMLStreamException {
      
      switch(this.basicIndex) {
      case 0:case 2:case 4:
         this.basicIndex++;
         break;
      case 1: case 3:
         if(this.checkedLinkIter.hasNext()) {
            this.checkedLink = this.checkedLinkIter.next();
            this.basicIndex = 2;
         }
         else {
            this.checkedLinkStream.close();
            this.basicIndex = 4;
         }
         break;
      }
         
      return BASIC_STRUCTURE[basicIndex];
   }

   @Override
   public int nextTag() throws XMLStreamException {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return 0;
   }

   @Override
   public void require(int arg0, String arg1, String arg2) throws XMLStreamException {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      
   }

   @Override
   public boolean standaloneSet() {
      log.error("method " + new Object() {}.getClass().getEnclosingMethod().getName() + "called but not implemented");
      return false;
   }
}

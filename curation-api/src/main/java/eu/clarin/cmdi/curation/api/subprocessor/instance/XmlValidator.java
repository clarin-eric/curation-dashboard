package eu.clarin.cmdi.curation.api.subprocessor.instance;

import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.Detail.Severity;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.XmlPopulationReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.XmlValidityReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.api.xml.CMDErrorHandler;
import eu.clarin.cmdi.curation.ccr.exception.CCRServiceNotAvailableException;
import eu.clarin.cmdi.curation.api.exception.MalFunctioningProcessorException;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.CRServiceStorageException;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.ValidatorHandler;
import java.io.IOException;

/**
 * The type Xml validator.
 */
@Slf4j
@Component
public class XmlValidator extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   @Autowired
   private CRService crService;

   /**
    * Process.
    *
    * @param instance the instance
    * @param report   the report
    */
   @Override
   public void process(CMDInstance instance, CMDInstanceReport report) throws MalFunctioningProcessorException {
      
      report.xmlPopulationReport = new XmlPopulationReport();
      

      ValidatorHandler schemaValidator;
      try {
         schemaValidator = crService.getSchema(report.profileHeaderReport.getProfileHeader().schemaLocation()).newValidatorHandler();
      }
      catch (NoProfileCacheEntryException e) {

         log.error("no ProfileCacheEntry for profile id '{}'", report.profileHeaderReport.getId());
         report.details.add(new Detail(Severity.FATAL, "xml-validation", "no ProfileCacheEntry for profile id '" + report.profileHeaderReport.getId() + "'"));
         report.isProcessable=false;
         
         return;
      }
      catch (CRServiceStorageException| CCRServiceNotAvailableException e) {

          throw new MalFunctioningProcessorException(e);
      }

       final int messageCount = report.details.size();

      schemaValidator.setErrorHandler(new CMDErrorHandler(report));
      schemaValidator.setContentHandler(new CMDInstanceContentHandler(instance, report));
      // setValidationFeatures(schemaValidator);
      SAXParserFactory parserFactory = SAXParserFactory.newInstance();
      parserFactory.setNamespaceAware(true);

      SAXParser parser;
      XMLReader reader;

      try {
         parser = parserFactory.newSAXParser();
         reader = parser.getXMLReader();
      }
      catch (ParserConfigurationException | SAXException e) {

         log.error("can't configure SAXParser for XML validation");
         throw new RuntimeException(e);

      }

      reader.setContentHandler(schemaValidator);
      try {
         reader.parse(new InputSource(instance.getPath().toUri().toString()));
      }
      catch (IOException e) {

         log.error("can't read input file '{}' for XML validation", instance.getPath());
         throw new RuntimeException(e);

      }
      catch (SAXException e) {

         log.error("can't parse input file '{}' for XML validation", instance.getPath());
         report.details.add(new Detail(Severity.FATAL, "xml-validation", "can't parse input file '" + instance.getPath().getFileName() + "'"));
         report.isProcessable=false;
         
         return;

      }
      
      if(report.xmlPopulationReport.numOfXMLSimpleElements > 0) {
         report.xmlPopulationReport.percOfPopulatedElements = 
               (double) (report.xmlPopulationReport.numOfXMLSimpleElements - report.xmlPopulationReport.numOfXMLEmptyElements)/report.xmlPopulationReport.numOfXMLSimpleElements;
      }
      
      report.xmlPopulationReport.score = report.xmlPopulationReport.percOfPopulatedElements;
      report.instanceScore+=report.xmlPopulationReport.score;

      report.xmlValidityReport = new XmlValidityReport();


      if(report.details.stream()
         .skip(messageCount)
         .filter(message -> (message.severity == Severity.FATAL || message.severity == Severity.ERROR))
         .count() < 3) {
         report.xmlValidityReport.score = 1.0;
         report.instanceScore+=report.xmlValidityReport.score;
      }
   }

   /**
    * The type Cmd instance content handler.
    */
   static class CMDInstanceContentHandler extends DefaultHandler {

      /**
       * The Instance.
       */
      CMDInstance instance;
      /**
       * The Instance report.
       */
      CMDInstanceReport instanceReport;


      /**
       * The Cur elem.
       */
      String curElem;
      /**
       * The Elem with value.
       */
      boolean elemWithValue;

      /**
       * The Locator.
       */
      Locator locator;

      /**
       * Instantiates a new Cmd instance content handler.
       *
       * @param instance       the instance
       * @param instanceReport the instance report
       */
      public CMDInstanceContentHandler(CMDInstance instance, CMDInstanceReport instanceReport) {
         
         this.instance = instance;
         this.instanceReport = instanceReport;
      
      }

      /**
       * Sets document locator.
       *
       * @param locator the locator
       */
      @Override
      public void setDocumentLocator(Locator locator) {
         this.locator = locator;
      }

      /**
       * Receive notification of the start of an element.
       *
       * @param uri        the uri
       * @param localName  the local name
       * @param qName      the q name
       * @param attributes the attributes
       * @throws SAXException the sax exception
       */
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

         this.instanceReport.xmlPopulationReport.numOfXMLElements++;
         curElem = qName;

      }

      /**
       * Receive notification of the end of an element.
       *
       * @param uri       the uri
       * @param localName the local name
       * @param qName     the q name
       * @throws SAXException the sax exception
       */
      public void endElement(String uri, String localName, String qName) throws SAXException {
         if (curElem.equals(qName)) {// is a simple elem
            this.instanceReport.xmlPopulationReport.numOfXMLSimpleElements++;
            if (!elemWithValue) {// does it have a value
               this.instanceReport.xmlPopulationReport.numOfXMLEmptyElements++;
               // deactivating warning until we made a decision on the subject
               //String msg = "Empty element <" + qName + "> was found on line " + locator.getLineNumber();
               //this.instanceReport.details.add(new Detail(Severity.WARNING, "xml-validation", msg));
            }
         }

         elemWithValue = false;
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
         elemWithValue = true;
      }

      /**
       * End document.
       *
       * @throws SAXException the sax exception
       */
      @Override
      public void endDocument() throws SAXException {
         // do nothing
      }
   }
}

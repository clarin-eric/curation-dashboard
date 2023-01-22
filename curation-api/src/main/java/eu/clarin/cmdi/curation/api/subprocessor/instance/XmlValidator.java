package eu.clarin.cmdi.curation.api.subprocessor.instance;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.ValidatorHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import eu.clarin.cmdi.curation.api.entity.CMDInstance;
import eu.clarin.cmdi.curation.api.report.Issue;
import eu.clarin.cmdi.curation.api.report.Issue.Severity;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.XmlPopulationReport;
import eu.clarin.cmdi.curation.api.report.instance.sec.XmlValidityReport;
import eu.clarin.cmdi.curation.api.subprocessor.AbstractSubprocessor;
import eu.clarin.cmdi.curation.api.xml.CMDErrorHandler;
import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.cr.exception.NoProfileCacheEntryException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class XmlValidator extends AbstractSubprocessor<CMDInstance, CMDInstanceReport> {

   @Autowired
   private CRService crService;

   @Override
   public void process(CMDInstance instance, CMDInstanceReport report) {
      
      report.xmlPopulationReport = new XmlPopulationReport();
      

      ValidatorHandler schemaValidator;
      try {
         schemaValidator = crService.getSchema(report.profileHeaderReport.getProfileHeader()).newValidatorHandler();
      }
      catch (NoProfileCacheEntryException e) {

         log.error("no ProfileCacheEntry for profile id '{}'", report.profileHeaderReport.getId());
         report.issues.add(new Issue(Severity.FATAL, "xml-validation", "no ProfileCacheEntry for profile id '" + report.profileHeaderReport.getId() + "'"));
         report.isValidReport=false;
         
         return;
      }
      
      final int messageCount = report.issues.size();

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
         report.issues.add(new Issue(Severity.FATAL, "xml-validation", "can't parse input file '" + instance.getPath().getFileName() + "'"));
         report.isValidReport=false;
         
         return;

      }
      
      if(report.xmlPopulationReport.numOfXMLSimpleElements > 0) {
         report.xmlPopulationReport.percOfPopulatedElements = 
               (double) (report.xmlPopulationReport.numOfXMLSimpleElements - report.xmlPopulationReport.numOfXMLEmptyElements)/report.xmlPopulationReport.numOfXMLSimpleElements;
      }
      
      report.xmlPopulationReport.score = report.xmlPopulationReport.percOfPopulatedElements;
      report.instanceScore+=report.xmlPopulationReport.score;

      report.xmlValidityReport = new XmlValidityReport();


      if(report.issues.stream()
         .skip(messageCount)
         .filter(message -> (message.severity == Severity.FATAL || message.severity == Severity.ERROR))
         .count() < 3) {
         report.xmlValidityReport.score = 1.0;
         report.instanceScore+=report.xmlValidityReport.score;
      }
   }

   class CMDInstanceContentHandler extends DefaultHandler {

      CMDInstance instance;
      CMDInstanceReport instanceReport;


      String curElem;
      boolean elemWithValue;

      Locator locator;

      public CMDInstanceContentHandler(CMDInstance instance, CMDInstanceReport instanceReport) {
         
         this.instance = instance;
         this.instanceReport = instanceReport;
      
      }

      @Override
      public void setDocumentLocator(Locator locator) {
         this.locator = locator;
      }

      /**
       * Receive notification of the start of an element.
       */
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

         this.instanceReport.xmlPopulationReport.numOfXMLElements++;
         curElem = qName;

      }

      /**
       * Receive notification of the end of an element.
       */
      public void endElement(String uri, String localName, String qName) throws SAXException {
         if (curElem.equals(qName)) {// is a simple elem
            this.instanceReport.xmlPopulationReport.numOfXMLSimpleElements++;
            if (!elemWithValue) {// does it have a value
               this.instanceReport.xmlPopulationReport.numOfXMLEmptyElements++;
               String msg = "Empty element <" + qName + "> was found on line " + locator.getLineNumber();
               this.instanceReport.issues.add(new Issue(Severity.WARNING, "xml-validation", msg));
            }
         }

         elemWithValue = false;
      }

      @Override
      public void characters(char[] ch, int start, int length) throws SAXException {
         elemWithValue = true;
      }

      @Override
      public void endDocument() throws SAXException {
         // do nothing
      }
   }
}

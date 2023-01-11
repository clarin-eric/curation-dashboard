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
import eu.clarin.cmdi.curation.api.report.Scoring.Severity;
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

   boolean valid = true;

   @Override
   public void process(CMDInstance instance, CMDInstanceReport report) {
      
      XmlPopulationReport populationReport = new XmlPopulationReport();
      report.setXmlPopulationReport(populationReport);

      ValidatorHandler schemaValidator;
      try {
         schemaValidator = crService.getSchema(report.getHeaderReport().getProfileHeader()).newValidatorHandler();
      }
      catch (NoProfileCacheEntryException e) {

         log.error("no ProfileCacheEntry for profile id '{}'", report.getHeaderReport().getId());
         populationReport.getScoring().addMessage(Severity.FATAL, "no ProfileCacheEntry for profile id '" + report.getHeaderReport().getId() + "'");
         return;
      }

      schemaValidator.setErrorHandler(new CMDErrorHandler(report));
      schemaValidator.setContentHandler(new CMDInstanceContentHandler(instance, populationReport));
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
         populationReport.getScoring().addMessage(Severity.FATAL, "can't parse input file '" + instance.getPath().getFileName() + "' ' for XML validation");
         return;

      }

      final XmlValidityReport validityReport = new XmlValidityReport();
      report.setXmlValidityReport(validityReport);

      populationReport.getScoring().getMessages().stream()
         .filter(message -> (message.getSeverity() == Severity.FATAL || message.getSeverity() == Severity.ERROR))
         .forEach(validityReport.getScoring().getMessages()::add);

   }

   class CMDInstanceContentHandler extends DefaultHandler {

      CMDInstance instance;
      XmlPopulationReport populationReport;


      String curElem;
      boolean elemWithValue;

      Locator locator;

      public CMDInstanceContentHandler(CMDInstance instance, XmlPopulationReport populationReport) {
         
         this.instance = instance;
         this.populationReport = populationReport;
      
      }

      @Override
      public void setDocumentLocator(Locator locator) {
         this.locator = locator;
      }

      /**
       * Receive notification of the start of an element.
       */
      public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

         this.populationReport.incrementNumOfXMLElements();
         curElem = qName;

         // handle attributes
         // TypeInfo etype = provider.getElementTypeInfo();
         // //sb.append(" of type {" + etype.getTypeNamespace() + '}' +
         // etype.getTypeName());
         // for (int a=0; a<attributes.getLength(); a++) {
         // TypeInfo atype = provider.getAttributeTypeInfo(a);
         // boolean spec = provider.isSpecified(a);
         // //sb.append("Attribute " + attributes.getQName(a) + (spec ? "
         // (specified)" : (" (defaulted)")));
         // if (atype == null) {
         // //sb.append(" of unknown type");
         // } else {
         // //sb.append(" of type {" + atype.getTypeNamespace() + '}' +
         // atype.getTypeName());
         // }
         // }
      }

      /**
       * Receive notification of the end of an element.
       */
      public void endElement(String uri, String localName, String qName) throws SAXException {
         if (curElem.equals(qName)) {// is a simple elem
            this.populationReport.incrementNumOfXMLSimpleElements();
            if (!elemWithValue) {// does it have a value
               this.populationReport.incrementNumOfXMLEmptyElements();
               String msg = "Empty element <" + qName + "> was found on line " + locator.getLineNumber();
               populationReport.getScoring().addMessage(Severity.WARNING, msg);
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

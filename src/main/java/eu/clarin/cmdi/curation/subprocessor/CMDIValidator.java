package eu.clarin.cmdi.curation.subprocessor;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.ValidatorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import eu.clarin.cmdi.curation.component_registry.XSDCache;
import eu.clarin.cmdi.curation.entities.CMDIRecord;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.xml.CMDIContentHandler;
import eu.clarin.cmdi.curation.xml.CMDIErrorHandler;

public class CMDIValidator extends CurationTask<CMDIRecord>{
	
	static final Logger _logger = LoggerFactory.getLogger(CMDIValidator.class);
	
	@Override
	public Report generateReport(CMDIRecord entity) {
		Report report = new Report("XSD Validation Report");
		try {
			ValidatorHandler schemaValidator = XSDCache.getInstance().getSchema(entity.getProfile()).newValidatorHandler();
			schemaValidator.setErrorHandler(new CMDIErrorHandler(report));
	        schemaValidator.setContentHandler(new CMDIContentHandler(entity, report));
	        //setValidationFeatures(schemaValidator);
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	        parserFactory.setNamespaceAware(true);
	        SAXParser parser = parserFactory.newSAXParser();
	        XMLReader reader = parser.getXMLReader();
	        reader.setContentHandler(schemaValidator);
	        reader.parse(new InputSource(entity.getPath().toUri().toString()));
		}catch(SAXException e){
		} catch (Exception e) {
			_logger.error("Error processing XSD schema with ID:  " + entity.getProfile(), e);
			report.addMessage(new Message(Severity.FATAL, "Error processing XSD schema with ID:  " + entity.getProfile(), e));
		}
		
		return report;
	}
	
	/*
	 * set custom xerces features here, if u really need them
	 * 
	 */
	
	private void setValidationFeatures(ValidatorHandler validator){
		try {
			validator.setFeature("http://apache.org/xml/features/warn-on-duplicate-entitydef", true);
			validator.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
			validator.setFeature("http://xml.org/sax/features/validation", true);
			validator.setFeature("http://apache.org/xml/features/validation/schema-full-checking", true);			
		} catch (Exception e) {
			_logger.warn("feature is not supported", e);
		}
	}
	
}

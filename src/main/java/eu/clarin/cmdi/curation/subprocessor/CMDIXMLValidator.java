package eu.clarin.cmdi.curation.subprocessor;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.ValidatorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import eu.clarin.cmdi.curation.component_registry.ComponentRegistryService;
import eu.clarin.cmdi.curation.entities.CMDIInstance;
import eu.clarin.cmdi.curation.report.CMDIInstanceReport;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.xml.CMDIContentHandler;
import eu.clarin.cmdi.curation.xml.CMDIErrorHandler;

public class CMDIXMLValidator extends CMDISubprocessor {

    static final Logger _logger = LoggerFactory.getLogger(CMDIXMLValidator.class);

    @Override
    public boolean process(CMDIInstance entity, CMDIInstanceReport report) {
	try {
	    ValidatorHandler schemaValidator = ComponentRegistryService.getInstance().getSchema(report.profile)
		    .newValidatorHandler();
	    schemaValidator.setErrorHandler(new CMDIErrorHandler(report));
	    schemaValidator.setContentHandler(new CMDIContentHandler(entity, report));
	    // setValidationFeatures(schemaValidator);
	    SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	    parserFactory.setNamespaceAware(true);
	    SAXParser parser = parserFactory.newSAXParser();
	    XMLReader reader = parser.getXMLReader();
	    reader.setContentHandler(schemaValidator);
	    reader.parse(new InputSource(entity.getPath().toUri().toString()));
	    return true;
	} catch (Exception e) {
	    _logger.error("Error processing XSD schema with ID:  " + report.profile, e);
	    report.addDetail(Severity.FATAL,
		    "Error processing XSD schema with ID:  " + report.profile + ", " + e.getMessage());
	    return false;
	}
    }

    /*
     * set custom xerces features here, if u really need them
     * 
     */

    private void setValidationFeatures(ValidatorHandler validator) {
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

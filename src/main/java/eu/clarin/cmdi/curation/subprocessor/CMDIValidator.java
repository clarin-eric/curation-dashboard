package eu.clarin.cmdi.curation.subprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import javax.xml.validation.ValidatorHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import eu.clarin.cmdi.curation.component_registry.XSDCache;
import eu.clarin.cmdi.curation.entities.CMDIRecord;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Severity;
import eu.clarin.cmdi.curation.xml.CMDIErrorHandler;

public class CMDIValidator implements CurationStep<CMDIRecord>{
	

	@Override
	public boolean apply(CMDIRecord entity) {
		
		Logger _logger = LoggerFactory.getLogger(CMDIValidator.class);
		String profileId = entity.getProfile();
		
		XSDCache profileCache = XSDCache.getInstance();	
		Validator validator;
		try {
			validator = profileCache.getSchemaValidator(profileId);			
		} catch (Exception e) {
			_logger.error("Error processing XSD schema with ID:  " + profileId, e);
			entity.addMessage(new Message(Severity.FATAL, 0, 0, "Error processing XSD schema with ID:  " + profileId, e));
			return false;
		}
		
		final ArrayList<Message> validationErrors = new ArrayList<Message>();
		validator.setErrorHandler(new CMDIErrorHandler(validationErrors));
		Source cmdiRecord = new StreamSource(entity.getPath().toFile());
		try {
			validator.validate(cmdiRecord);
		} catch (SAXException e) {
			_logger.error("CMDI Record " + entity.getPath() + " is not valid", e);
			
		} catch (IOException e) {
			_logger.error("Error processing CMDI record " + entity.getPath(), e);
		}
		
		validator.reset();
		
		
		if(validationErrors.isEmpty())
			return true;
		
		validationErrors.forEach(msg -> entity.addMessage(msg));		
		Severity highest = Collections.max(validationErrors).getSeverity();
		return highest.compareTo(Severity.WARNING) <= 0;
		
	}
	
	
	public void validate(CMDIRecord entity){		
		XSDCache schemasCache = XSDCache.getInstance();
		try {
			ValidatorHandler schemaValidator = schemasCache.getSchemaValidatorHandler(entity.getProfile());
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	        parserFactory.setNamespaceAware(true);
	        SAXParser parser = parserFactory.newSAXParser();
	        XMLReader reader = parser.getXMLReader();
	        reader.setContentHandler(schemaValidator);
	        reader.parse(new InputSource(entity.getPath().toUri().toString()));
        
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

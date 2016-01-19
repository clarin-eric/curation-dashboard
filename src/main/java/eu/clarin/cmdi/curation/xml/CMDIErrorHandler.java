package eu.clarin.cmdi.curation.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 * 
 * A custom SAX error handler
 */

public class CMDIErrorHandler implements ErrorHandler{
	
	Report report;
	
	public CMDIErrorHandler(Report report){
		this.report = report;
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		report.addMessage(new Message(Severity.FATAL, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage(), null)); //, exception
		throw exception;
	}
	
	@Override
	public void error(SAXParseException exception) throws SAXException {
		report.addMessage(new Message(Severity.ERROR, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage(), null)); //, exception
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		report.addMessage(new Message(Severity.WARNING, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage(), null)); //, exception
		
	}
}

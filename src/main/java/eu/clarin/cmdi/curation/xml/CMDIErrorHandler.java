package eu.clarin.cmdi.curation.xml;

import java.util.Collection;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 * 
 * A custom SAX error handler
 */

public class CMDIErrorHandler implements ErrorHandler{

	private final Collection<Message> messages;
	
	public CMDIErrorHandler(Collection<Message> messages){
		this.messages = messages;
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		messages.add(new Message(Severity.FATAL, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage(), exception));
		throw exception; 
	}
	
	@Override
	public void error(SAXParseException exception) throws SAXException {
		messages.add(new Message(Severity.ERROR, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage(), exception));			
	}

	@Override
	public void warning(SAXParseException exception) throws SAXException {
		messages.add(new Message(Severity.WARNING, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage(), exception));
		
	}
}

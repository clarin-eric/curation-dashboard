package eu.clarin.cmdi.curation.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.clarin.cmdi.curation.entities.CMDIRecord;
import eu.clarin.cmdi.curation.report.Detail;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 * 
 *         A custom SAX error handler
 */

public class CMDIErrorHandler implements ErrorHandler {

    CMDIRecord instance;

    public CMDIErrorHandler(CMDIRecord instance) {
	this.instance = instance;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
	addMessage(Severity.FATAL, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
	throw exception;
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
	addMessage(Severity.ERROR, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
	addMessage(Severity.WARNING, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());

    }

    private void addMessage(Severity lvl, int line, int col, String msg) {
	instance.addDetail(lvl, 	"line: " + line + ", col: " + col + " - " + msg);

    }
}

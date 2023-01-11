package eu.clarin.cmdi.curation.api.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.clarin.cmdi.curation.api.report.Scoring.Severity;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;

/**

 * 
 *         A custom SAX error handler
 */

public class CMDErrorHandler implements ErrorHandler {

	CMDInstanceReport report;

	public CMDErrorHandler(CMDInstanceReport report) {
		this.report = report;
	}

	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		addMessage(Severity.FATAL, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
		throw exception;
	}

	@Override
	public void error(SAXParseException exception){
		addMessage(Severity.ERROR, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
	}

	@Override
	public void warning(SAXParseException exception) {
		addMessage(Severity.WARNING, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());

	}

	private void addMessage(Severity lvl, int line, int col, String message) {
		report.getScoring().addMessage(lvl, "line: " + line + ", col: " + col + " - " + message);
	}
}

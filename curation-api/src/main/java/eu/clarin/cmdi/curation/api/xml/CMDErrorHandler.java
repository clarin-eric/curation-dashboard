package eu.clarin.cmdi.curation.api.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import eu.clarin.cmdi.curation.api.report.Detail;
import eu.clarin.cmdi.curation.api.report.Detail.Severity;
import eu.clarin.cmdi.curation.api.report.instance.CMDInstanceReport;

/**
 * A custom SAX error handler
 */
public class CMDErrorHandler implements ErrorHandler {

	/**
	 * The Report.
	 */
	CMDInstanceReport report;

	/**
	 * Instantiates a new Cmd error handler.
	 *
	 * @param report the report
	 */
	public CMDErrorHandler(CMDInstanceReport report) {
		this.report = report;
	}

	/**
	 * Fatal error.
	 *
	 * @param exception the exception
	 * @throws SAXException the sax exception
	 */
	@Override
	public void fatalError(SAXParseException exception) throws SAXException {
		addMessage(Severity.FATAL, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
		throw exception;
	}

	/**
	 * Error.
	 *
	 * @param exception the exception
	 */
	@Override
	public void error(SAXParseException exception){
		addMessage(Severity.ERROR, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
	}

	/**
	 * Warning.
	 *
	 * @param exception the exception
	 */
	@Override
	public void warning(SAXParseException exception) {
		addMessage(Severity.WARNING, exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());

	}

	private void addMessage(Severity lvl, int line, int col, String message) {
		report.details.add(new Detail(lvl, "xml-validation", "line: " + line + ", col: " + col + " - " + message));
	}
}

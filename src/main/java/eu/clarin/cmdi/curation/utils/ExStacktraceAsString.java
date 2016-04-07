package eu.clarin.cmdi.curation.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExStacktraceAsString {

	public synchronized static String getStackTrace(final Throwable ex) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw, true);
		ex.printStackTrace(pw);
		return sw.getBuffer().toString();
	}

}

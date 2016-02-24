package eu.clarin.cmdi.curation.subprocessor;

import java.util.concurrent.atomic.AtomicInteger;

import eu.clarin.cmdi.curation.entities.CMDIInstance;
import eu.clarin.cmdi.curation.io.HTTPLinkChecker;
import eu.clarin.cmdi.curation.report.CMDIInstanceReport;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *
 */

public class URLValidator extends CMDISubprocessor {

    @Override
    public boolean process(CMDIInstance entity, CMDIInstanceReport report) {
	AtomicInteger numOfBrokenLinks = new AtomicInteger(0);

	//links are unique
	entity.getLinks().parallelStream().forEach(value -> {
	    String link = value.getValue();
	    String tag = value.getTag() != null ? value.getTag() + ": " : "";

	    try {// check if URL is broken
		 // we don't care about errors because of invalid certificatesF
		String url = value.getValue().replaceFirst("^https", "http");

		int responseCode = new HTTPLinkChecker().checkLink(url);

		if (responseCode == 200 || responseCode == 302) {
		    // debug info
		    // report.addMessage(new Message(Severity.DEBUG, tag + "URL:
		    // " + link + "\t STATUS:" + responseCode));
		} // OK
		else if (responseCode < 400)// 2XX and 3XX, redirections, empty
					    // content ...
		    report.addDetail(Severity.WARNING, tag + "URL: " + link + "\t STATUS:" + responseCode);

		else {// 4XX and 5XX, client/server errors
		    numOfBrokenLinks.incrementAndGet();
		    report.addDetail(Severity.ERROR, tag + "URL: " + link + "\t STATUS:" + responseCode);
		}
	    } catch (Exception e) {
		numOfBrokenLinks.incrementAndGet();
		report.addDetail(Severity.ERROR, tag + "URL: " + link + "\t STATUS:" + e.getMessage());
	    }
	});
	
	report.numOfBrokenLinks = numOfBrokenLinks.get();	
	return true;

    }


}

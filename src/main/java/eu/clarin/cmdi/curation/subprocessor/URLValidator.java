package eu.clarin.cmdi.curation.subprocessor;

import java.util.concurrent.atomic.AtomicInteger;

import eu.clarin.cmdi.curation.entities.CMDIInstance;
import eu.clarin.cmdi.curation.io.HTTPLinkChecker;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDIInstanceReport;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *
 */

public class URLValidator extends CMDISubprocessor {

    @Override
    public boolean process(CMDIInstance entity, CMDIInstanceReport report) {
	// links are unique
	if (Configuration.HTTP_VALIDATION) {
	    AtomicInteger numOfBrokenLinks = new AtomicInteger(0);
	    entity.getLinks().parallelStream().forEach(value -> {
		String link = value.getValue();
		String tag = value.getTag() != null ? value.getTag() + ": " : "";

		try {// check if URL is broken
		     // we don't care about errors because of invalid
		     // certificatesF
		    String url = value.getValue().replaceFirst("^https", "http");

		    int responseCode = new HTTPLinkChecker().checkLink(url);

		    if (responseCode == 200 || responseCode == 302) {
			// debug info
			// report.addMessage(new Message(Severity.DEBUG, tag +
			// "URL:
			// " + link + "\t STATUS:" + responseCode));
		    } // OK
		    else if (responseCode < 400)// 2XX and 3XX, redirections,
						// empty
						// content ...
			addMessage(Severity.WARNING, tag + "URL: " + link + "\t STATUS:" + responseCode);

		    else {// 4XX and 5XX, client/server errors
			numOfBrokenLinks.incrementAndGet();
			addMessage(Severity.ERROR, tag + "URL: " + link + "\t STATUS:" + responseCode);
		    }
		} catch (Exception e) {
		    numOfBrokenLinks.incrementAndGet();
		    addMessage(Severity.ERROR, tag + "URL: " + link + "\t STATUS:" + e.getMessage());
		}
	    });
	    
	    report.addURLReport(numOfBrokenLinks.get(), msgs);
	}else
	    report.addURLReport(0, msgs);

	
	return true;

    }

}

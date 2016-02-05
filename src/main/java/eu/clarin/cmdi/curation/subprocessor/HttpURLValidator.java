/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import eu.clarin.cmdi.curation.entities.CMDIRecord;
import eu.clarin.cmdi.curation.entities.CMDIUrlNode;
import eu.clarin.cmdi.curation.io.HTTPLinkChecker;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *
 */

public class HttpURLValidator implements ProcessingActivity<CMDIRecord> {

    Severity highest = Severity.NONE;

    @Override
    public Severity process(CMDIRecord entity) {
	int totalNumberOfLinks;
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
		    addMessage(entity, Severity.WARNING, tag + "URL: " + link + "\t STATUS:" + responseCode);

		else {// 4XX and 5XX, client/server errors
		    numOfBrokenLinks.incrementAndGet();
		    addMessage(entity, Severity.ERROR, tag + "URL: " + link + "\t STATUS:" + responseCode);
		}
	    } catch (Exception e) {
		numOfBrokenLinks.incrementAndGet();
		addMessage(entity, Severity.ERROR, tag + "URL: " + link + "\t STATUS:" + e.getMessage());
	    }
	});
	entity.setNumOfBrokenLinks(numOfBrokenLinks.get());
	
	return highest;

    }

    private void addMessage(CMDIRecord entity, Severity lvl, String msg) {
	entity.addDetail(lvl, msg);
	if (lvl.compareTo(highest) > 0)
	    highest = lvl;
    }

}

package eu.clarin.cmdi.curation.subprocessor;

import java.util.concurrent.atomic.AtomicInteger;

import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.io.HTTPLinkChecker;
import eu.clarin.cmdi.curation.main.Configuration;
import eu.clarin.cmdi.curation.report.CMDInstanceReport;
import eu.clarin.cmdi.curation.report.CMDInstanceReport.URLReport;
import eu.clarin.cmdi.curation.report.Score;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *
 */

public class URLValidator extends CMDSubprocessor {

    @Override
    public void process(CMDInstance entity, CMDInstanceReport report) {
		// links are unique
		if (Configuration.HTTP_VALIDATION) {
		    AtomicInteger numOfBrokenLinks = new AtomicInteger(0);
		    entity.getLinks().stream().forEach(value -> {
				String link = value.getValue();
				String tag = value.getTag() != null ? value.getTag() + ": " : "";
		
				try {// check if URL is broken					
				    int responseCode = new HTTPLinkChecker().checkLink(value.getValue());		
				    if (responseCode == 200 || responseCode == 302) {
// debug info
// report.addMessage(new Message(Severity.DEBUG, tag + "URL: " + link + "\t STATUS:" + responseCode));
				    } // OK
				    else if (responseCode < 400)// 2XX and 3XX, redirections, empty content
				    	addMessage(Severity.WARNING, tag + "URL: " + link + "     STATUS:" + responseCode);		
				    else {// 4XX and 5XX, client/server errors
						numOfBrokenLinks.incrementAndGet();
						addMessage(Severity.ERROR, tag + "URL: " + link + "    STATUS:" + responseCode);
				    }
				} catch (Exception e) {
				    numOfBrokenLinks.incrementAndGet();
				    addMessage(Severity.ERROR, tag + "URL: " + link + "    STATUS:" + e.toString());
				}
		    });
		    report.urlReport = createURLReport(report.numOfLinks, numOfBrokenLinks.get(), report.numOfResProxiesLinks, report.numOfUniqueLinks);
		}else{
			 report.urlReport = createURLReport(report.numOfLinks, 0, report.numOfResProxiesLinks, report.numOfUniqueLinks);
			 addMessage(Severity.INFO, "Link validation is disabled");
		}
    }
    
	@Override
	public Score calculateScore(CMDInstanceReport report) {
		// it can influence the score, if one collection was done with enabled and the other without
		
		double score = !Double.isNaN(report.urlReport.percOfValidLinks)? report.urlReport.percOfValidLinks : 0;
		return new Score(score, 1.0, "url-validation", msgs);
	}	
    
    private URLReport createURLReport(int numOfLinks, int numOfBrokenLinks, int numOfResProxiesLinks, int numOfUniqueLinks) {
    	URLReport report = new URLReport();
    	report.numOfLinks = numOfLinks;
    	report.numOfBrokenLinks = numOfBrokenLinks;
    	report.numOfResProxiesLinks = numOfResProxiesLinks;
    	report.numOfUniqueLinks = numOfUniqueLinks;
    	report.percOfValidLinks = Double.NaN;		
    	if (Configuration.HTTP_VALIDATION)
    		report.percOfValidLinks = (numOfUniqueLinks - numOfBrokenLinks) / (double) numOfUniqueLinks;
    	
    	return report;
	}

}

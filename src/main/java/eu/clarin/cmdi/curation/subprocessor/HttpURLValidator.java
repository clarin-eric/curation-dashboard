/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import eu.clarin.cmdi.curation.entities.CMDIRecord;
import eu.clarin.cmdi.curation.entities.CMDIRecordValue;
import eu.clarin.cmdi.curation.io.HTTPLinkChecker;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *
 */
public class HttpURLValidator extends CurationTask<CMDIRecord>{
	

	@Override
	public Report generateReport(CMDIRecord entity) {
		Report report = new Report("HttpLinks Report");
		int totalNumberOfLinks;
		AtomicInteger numOfBrokenLinks = new AtomicInteger(0);		
		
		//filter urls
		Collection<CMDIRecordValue> urls = entity
				.getValues()
				.stream()
				.filter(value -> value.getValue().startsWith("http://") || value.getValue().startsWith("https://"))
				.collect(Collectors.toList());
		
		totalNumberOfLinks = urls.size();
		
		//filter unique urls
		urls = urls.stream().distinct().collect(Collectors.toList());		
		
		urls.parallelStream().forEach(value -> {
			String link = value.getValue();
			String tag = value.getTag() != null? value.getTag() + ": " : "";
			
			try{//check if URL is broken
				String url = value.getValue().replaceFirst("^https", "http"); //we don't care about errors because of invalid certificates
				
				int responseCode = new HTTPLinkChecker().checkLink(url);
				
				if(responseCode == 200 || responseCode == 302){
					report.addDebugMessage(tag + "URL: " + link + "\t STATUS:" + responseCode);
				} //OK
				else if(responseCode < 400)//2XX and 3XX, redirections, empty content ...
					report.addMessage(new Message(Severity.WARNING, tag + "URL: " + link + "\t STATUS:" + responseCode));
				else{//4XX and 5XX, client/server errors
					numOfBrokenLinks.incrementAndGet();
					report.addMessage(new Message(Severity.ERROR, tag + "URL: " + link + "\t STATUS:" + responseCode));
				}
			}catch(Exception e){
				numOfBrokenLinks.incrementAndGet();
				report.addMessage(new Message(Severity.ERROR, tag + "URL: " + link + "\t STATUS:" + e.getMessage()));
			}
		});
		
		report.addMessage("Total number of links: " + totalNumberOfLinks);
		report.addMessage("Total number of unique links: " + urls.size());
		report.addMessage("Number of broken links: " + numOfBrokenLinks + "\t" + percent(numOfBrokenLinks.get(), urls.size()));
		return report;
	
	}
	
	private String percent(double a, double b){
		return new DecimalFormat("0.00").format(100 * a/b) + "%";
	}


}

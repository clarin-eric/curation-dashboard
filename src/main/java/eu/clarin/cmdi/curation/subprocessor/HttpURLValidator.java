/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicInteger;

import eu.clarin.cmdi.curation.entities.CMDIRecord;
import eu.clarin.cmdi.curation.report.Message;
import eu.clarin.cmdi.curation.report.Report;
import eu.clarin.cmdi.curation.report.Severity;

/**
 * @author dostojic
 *
 */
public class HttpURLValidator extends CurationStep<CMDIRecord>{
	
	int timeout;
	
	public HttpURLValidator(){
		this(1000);//1 sec by default
	}
	
	public HttpURLValidator(int timeout){
		this.timeout = timeout;
	}

	@Override
	public Report process(CMDIRecord entity) {
		Report report = new Report("HttpLinks Report");
		AtomicInteger numOfLinks = new AtomicInteger(0);
		AtomicInteger numOfBrokenLinks = new AtomicInteger(0);
		entity.getValues()
		.parallelStream()
		.forEach(value -> {
			if(value.startsWith("http://") || value.startsWith("https://")){
				numOfLinks.incrementAndGet();				
				
				try{//check if URL is broken
					String url = value.replaceFirst("^https", "http"); //we don't care about errors because of invalid certificates
					HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
					connection.setConnectTimeout(timeout);
					connection.setReadTimeout(timeout);
					connection.setRequestMethod("HEAD");
					int responseCode = connection.getResponseCode();
					if (responseCode != 200) {
						numOfBrokenLinks.incrementAndGet();
						report.addMessage(new Message(Severity.ERROR, "URL: " + value + "\t STATUS:" + responseCode));
					}
				}catch(Exception e){
					numOfBrokenLinks.incrementAndGet();
					report.addMessage(new Message(Severity.ERROR, "URL: " + value + "\t STATUS:" + e.getMessage()));
				}
				
			}
		});
		report.addMessage(new Message("Total number of links: " + numOfLinks));
		report.addMessage(new Message("Number of broken links: " + numOfBrokenLinks + "\t"
				+ new DecimalFormat("0.00").format((double)numOfBrokenLinks.get()/(double)numOfLinks.get() + "%")));
		return report;
	
	}


}

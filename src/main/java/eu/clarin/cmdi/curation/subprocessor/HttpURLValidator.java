/**
 * 
 */
package eu.clarin.cmdi.curation.subprocessor;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
		this(2000);//1 sec by default
	}
	
	public HttpURLValidator(int timeout){
		this.timeout = timeout;
	}

	@Override
	public Report process(CMDIRecord entity) {
		Report report = new Report("HttpLinks Report");
		int totalNumberOfLinks;
		AtomicInteger numOfBrokenLinks = new AtomicInteger(0);		
		
		//filter urls
		Collection<String> urls = entity
				.getValues()
				.stream()
				.filter(value -> value.startsWith("http://") || value.startsWith("https://"))
				.collect(Collectors.toList());
		
		totalNumberOfLinks = urls.size();
		
		//filter unique urls
		urls = urls.stream().distinct().collect(Collectors.toList());
		
		
		urls.parallelStream()
		.forEach(value -> {
			try{//check if URL is broken
				String url = value.replaceFirst("^https", "http"); //we don't care about errors because of invalid certificates
				HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
				connection.setConnectTimeout(timeout);
				connection.setReadTimeout(timeout);
				connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0");
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

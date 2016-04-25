package eu.clarin.cmdi.curation.io;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author dostojic
 *
 */
public class HTTPLinkChecker{
	
	static final int timeout = 5000;
	private HttpURLConnection connection = null;
	private String redirectLink = null;
	
	
	public int checkLink(String url)throws Exception{
		connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");		
		connection.setRequestMethod("GET");
		String redirectLink = connection.getHeaderField("Location");				
		if (redirectLink != null && !redirectLink.equals(url)){
			this.redirectLink = redirectLink;
			return checkLink(redirectLink);
		}
		else
			return connection.getResponseCode();
	}
	
	public String getRedirectLink(){
		return redirectLink;
	}
	
	
	public String getResponse() throws IOException{
		if(connection == null)
			return "Connection is null";
		
		StringBuilder builder = new StringBuilder();
		builder.append(connection.getResponseCode())
		       .append(" ")
		       .append(connection.getResponseMessage())
		       .append("\n");

		Map<String, List<String>> map = connection.getHeaderFields();
		for (Map.Entry<String, List<String>> entry : map.entrySet())
		{
		    if (entry.getKey() == null) 
		        continue;
		    builder.append( entry.getKey())
		           .append(": ");

		    List<String> headerValues = entry.getValue();
		    Iterator<String> it = headerValues.iterator();
		    if (it.hasNext()) {
		        builder.append(it.next());

		        while (it.hasNext()) {
		            builder.append(", ")
		                   .append(it.next());
		        }
		    }

		    builder.append("\n");
		}
		
		return builder.toString();
	}
	
//	public static void main(String[] args) throws Exception{
//		System.setProperty("jsse.enableSNIExtension", "false");		
//		HTTPLinkChecker l = new HTTPLinkChecker();
//		l.checkLink("http://hdl.handle.net/11022/0000-0000-63C6-1@PDF");
//		System.out.println(l.getResponse());
//	}
	
}

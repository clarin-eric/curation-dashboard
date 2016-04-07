package eu.clarin.cmdi.curation.io;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author dostojic
 *
 */
public class HTTPLinkChecker{

	static final int timeout = 2000;
	
	public int checkLink(String url)throws Exception{
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setConnectTimeout(timeout);
		connection.setReadTimeout(timeout);
		connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:43.0) Gecko/20100101 Firefox/43.0");
		connection.setRequestMethod("HEAD");
		return connection.getResponseCode();
	}

}

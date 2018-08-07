/**
 * 
 */
package eu.clarin.cmdi.curation.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author dostojic
 *
 */
public class Downloader {

	static final Logger logger = LoggerFactory.getLogger(Downloader.class);

	public void download(String url, File destination) throws IOException {		
		try {
			HTTPLinkChecker linkChecker = new HTTPLinkChecker(15000); //custom timeout for slow CR
			int statusCode = linkChecker.checkLink(url,0);
			if(statusCode != 200 && statusCode != 304){
				throw new Exception(url + " is not valid! Response from server:\n" + linkChecker.getResponse());
			}

			logger.trace("Downloading file from {} into {}", url, destination.getName());
			
			if(linkChecker.getRedirectLink() != null){
				url = linkChecker.getRedirectLink();
			}
			
			ReadableByteChannel channel = Channels.newChannel(new URL(url).openStream());
			FileOutputStream fos = new FileOutputStream(destination);
			long size = fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
			logger.trace("File successefully downloaded with size {} KB",
					new DecimalFormat("#,##0.#").format(size / 1024));
			fos.close();
		} catch (Exception e) {
			logger.warn("error while downloading file {}, deleting local file {}", url, destination.getName());
			destination.delete();
			throw new IOException("Error while downloading file " + url, e);
		}
	}

}

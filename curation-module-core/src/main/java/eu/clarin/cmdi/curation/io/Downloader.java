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

	static final Logger _logger = LoggerFactory.getLogger(Downloader.class);

	public void download(String url, File destination) throws IOException {
		try {
			int responseCode = new HTTPLinkChecker().checkLink(url);
			
			if(responseCode != 200)
				throw new Exception("HTTP status code was " + responseCode + ". Check if " + url + " is a valid URL");
			
			_logger.trace("Downloading file from {} into {}", url, destination.getName());
			ReadableByteChannel channel = Channels.newChannel(new URL(url).openStream());
			FileOutputStream fos = new FileOutputStream(destination);
			long size = fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
			_logger.trace("File successefully downloaded with size {} KB",
					new DecimalFormat("#,##0.#").format(size / 1024));
			fos.close();
		} catch (Exception e) {
			throw new IOException("Error while downloading file " + url, e);
		}
	}

}

package eu.clarin.cmdi.curation.io;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Downloader implements Callable<Void> {
	
	private static final Logger _logger = LoggerFactory.getLogger(Downloader.class);
	
	private String url;
	private String dest;
	
	
	
	public Downloader(String url, String dest) {
		this.url = url;
		this.dest = dest;
	}
	
	
	public void download() throws IOException{
		call();
	}
	

	@Override
	public Void call() throws IOException{			
		_logger.trace("Downloading file from {} into {}", url, dest);
		try {
			ReadableByteChannel channel = Channels.newChannel(new URL(url).openStream());
			FileOutputStream fos = new FileOutputStream(dest);			
	    	long size = fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
			_logger.trace("File successefully downloaded with size {} KB", new DecimalFormat("#,##0.#").format(size/1024));
			fos.close();
			return null;
		} catch (IOException e) {
			_logger.error("Error while downloading file {}", url);
			throw e;
		}			
    	
	}
}

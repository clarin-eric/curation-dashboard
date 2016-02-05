package eu.clarin.cmdi.curation.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.component_registry.ComponentRegistryService;

public class DownloaderService {

    static final Logger _logger = LoggerFactory.getLogger(DownloaderService.class);

    // Singleton
    static DownloaderService instance = new DownloaderService();

    public static DownloaderService getInstance() {
	return instance;
    }


    private DownloaderService() {}

    public void download(String url, File destination) throws Exception {
	DownloadTask task = new DownloadTask(url, destination);
	new Thread(task).start();
	if(task.exception != null)
	    throw task.exception;
    }

    public static class DownloadTask implements Runnable {

	private String url;
	private File destination;
	
	Exception exception = null;

	public DownloadTask(String url, File destination) {
	    this.url = url;
	    this.destination = destination;
	}

	@Override
	public void run(){
	    long threadId = Thread.currentThread().getId();
	    _logger.info("Thread: {} started", threadId);
	    _logger.trace("Downloading file from {} into {}", url, destination.getName());
	    try {
		ReadableByteChannel channel = Channels.newChannel(new URL(url).openStream());
		FileOutputStream fos = new FileOutputStream(destination);
		long size = fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
		_logger.trace("File successefully downloaded with size {} KB",
			new DecimalFormat("#,##0.#").format(size / 1024));
		fos.close();
		_logger.info("Thread: {} finished", threadId);
	    } catch (IOException e) {
		_logger.error("Error while downloading file {}", url);
		exception = e;
	    }
	}

    }

    
    //test concurency
    public static void main(String[] args) throws Exception {
	String[] profiles = { "clarin.eu:cr1:p_1357720977520", "clarin.eu:cr1:p_1361876010587",
		"clarin.eu:cr1:p_1297242111880" };

	for (String p : profiles) {
	    String url = ComponentRegistryService.CCR_REST + p + "/xsd";
	    String dest = ComponentRegistryService.SCHEMA_FOLDER + p.substring("clarin.eu:cr1:".length()) + ".xsd";
	    instance.download(url, new File(dest));
	}

    }

}

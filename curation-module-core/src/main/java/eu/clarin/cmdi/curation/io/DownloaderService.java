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

public class DownloaderService {

	static final Logger logger = LoggerFactory.getLogger(DownloaderService.class);

	// Singleton
	static DownloaderService instance = new DownloaderService();

	public static DownloaderService getInstance() {
		return instance;
	}

	private DownloaderService() {
	}

	public void download(String url, File destination) throws Exception {
		DownloadTask task = new DownloadTask(url, destination);
		new Thread(task).start();
		if (task.exception != null)
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
		public void run() {
			long threadId = Thread.currentThread().getId();
			logger.info("Thread: {} started", threadId);
			logger.trace("Downloading file from {} into {}", url, destination.getName());
			try {
				ReadableByteChannel channel = Channels.newChannel(new URL(url).openStream());
				FileOutputStream fos = new FileOutputStream(destination);
				long size = fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
				logger.trace("File successefully downloaded with size {} KB",
						new DecimalFormat("#,##0.#").format(size / 1024));
				fos.close();
				logger.info("Thread: {} finished", threadId);
			} catch (IOException e) {
				logger.error("Error while downloading file {}", url);
				exception = e;
			}
		}

	}

}

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

import eu.clarin.cmdi.curation.main.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.curation.linkchecker.httpLinkChecker.HTTPLinkChecker;

/**
 * @author dostojic
 */
public class Downloader {

    private static final Logger _logger = LoggerFactory.getLogger(Downloader.class);

    public void download(String url, File destination) throws IOException {
        try {
            HTTPLinkChecker linkChecker = new HTTPLinkChecker(15000,5, Configuration.USERAGENT); //custom timeout for slow CR
            int statusCode = linkChecker.checkLinkAndGetResponseCode(url);
            if (statusCode != 200 && statusCode != 304) {
                throw new Exception(url + " is not valid! Response from server:\n" + linkChecker.getResponse());
            }

            _logger.trace("Downloading file from {} into {}", url, destination.getName());

            if (linkChecker.getRedirectLink() != null) {
                url = linkChecker.getRedirectLink();
            }

            ReadableByteChannel channel = Channels.newChannel(new URL(url).openStream());
            FileOutputStream fos = new FileOutputStream(destination);
            long size = fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
            _logger.trace("File successefully downloaded with size {} KB",
                    new DecimalFormat("#,##0.#").format(size / 1024));
            fos.close();
        } catch (Exception e) {
            _logger.warn("error while downloading file {}, deleting local file {}", url, destination.getName());
            destination.delete();
            throw new IOException("Error while downloading file " + url, e);
        }
    }

}

package eu.clarin.cmdi.curation.cache;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.component_registry.ComponentRegistryService;

public class XSDCache extends AbstractCache<String, Schema> {

    private static final Logger _logger = LoggerFactory.getLogger(XSDCache.class);

    // Singleton
    private static XSDCache instance = new XSDCache();

    private List<String> beingLoadedSchemas = new LinkedList<String>();

    private Map<String, Exception> exceptions = new HashMap<>();

    private XSDCache() {
    }

    public static XSDCache getInstance() {
	return instance;
    }

    public Schema lookup(final String profile) throws Exception {
	synchronized (cache) {
	    if (!cache.containsKey(profile)) {
		if (!beingLoadedSchemas.contains(profile)) {
		    beingLoadedSchemas.add(profile);
		    new Thread() {// if not in the cache and not being currently
				  // loaded start new Thread
			public void run() {
			    try {
				Path pathToSchema = Paths.get(ComponentRegistryService.SCHEMA_FOLDER, profile + ".xsd");
				_logger.trace("Loading {} schema from {}", profile, pathToSchema);

				if (Files.notExists(pathToSchema)) {
				    _logger.trace("Schema for {} is not in the local FS, downloading it", profile);
				    String profileURL = ComponentRegistryService.CCR_REST + ComponentRegistryService.PROFILE_PREFIX + profile + "/xsd";
				    Downloader downloader = new Downloader(profileURL,
					    ComponentRegistryService.SCHEMA_FOLDER + "/" + profile + ".xsd");
				    downloader.download();
				}

				SchemaFactory schemaFactory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
				Schema schema = schemaFactory.newSchema(pathToSchema.toFile());

				synchronized (cache) {
				    cache.put(profile, schema);
				    cache.notifyAll();
				    beingLoadedSchemas.remove(profile);
				}

			    } catch (Exception e) {
				synchronized (cache) {
				    cache.put(profile, null);
				    cache.notifyAll();
				    // save the exception goes into report
				    exceptions.put(profile, e);
				}
			    }

			}
		    }.start();
		}
	    }

	    while (!cache.containsKey(profile))
		cache.wait();

	}

	Schema schema = cache.get(profile);
	if (schema == null)
	    throw exceptions.get(profile);

	return schema;

    }

    class Downloader {
	private String url;
	private String dest;

	public Downloader(String url, String dest) {
	    this.url = url;
	    this.dest = dest;
	}

	public void download() throws IOException {
	    _logger.trace("Downloading file from {} into {}", url, dest);
	    try {
		ReadableByteChannel channel = Channels.newChannel(new URL(url).openStream());
		FileOutputStream fos = new FileOutputStream(dest);
		long size = fos.getChannel().transferFrom(channel, 0, Long.MAX_VALUE);
		_logger.trace("File successefully downloaded with size {} KB",
			new DecimalFormat("#,##0.#").format(size / 1024));
		fos.close();
	    } catch (IOException e) {
		_logger.error("Error while downloading file {}", url);
		throw e;
	    }
	}
    }

}

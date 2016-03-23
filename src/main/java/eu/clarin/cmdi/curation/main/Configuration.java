package eu.clarin.cmdi.curation.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

	private static Logger _logger = LoggerFactory.getLogger(Configuration.class);

	public static final String SCORE_NUMERIC_DISPLAY_FORMAT = "#0.00";

	public static long MAX_FILE_SIZE;
	public static boolean HTTP_VALIDATION;
	public static boolean GENERATE_CHILDREN_REPORTS;
	public static Path OUTPUT_DIRECTORY = null;
	public static Path CACHE_DIRECTORY = null;

	public static void init(File file) throws ConfigurationException, IOException {
		_logger.info("Initializing configuration from {}", file);
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		MAX_FILE_SIZE = config.getLong("MAX_FILE_SIZE");
		HTTP_VALIDATION = config.getBoolean("HTTP_VALIDATION");
		GENERATE_CHILDREN_REPORTS = config.getBoolean("GENERATE_CHILDREN_REPORTS");
		
		String outDir = config.getString("OUTPUT_DIRECTORY");
		String cacheDir = config.getString("CACHE_DIRECTORY");
		
		if(outDir != null)
			OUTPUT_DIRECTORY = Files.createDirectories(Paths.get(outDir));
		if(cacheDir != null)
			CACHE_DIRECTORY = Files.createDirectories(Paths.get(cacheDir));
	}
	
	public static void initDefault() throws ConfigurationException, IOException {
		_logger.info("Initializing configuration from {}", "config.properties");
		PropertiesConfiguration config = new PropertiesConfiguration("config.properties");
		MAX_FILE_SIZE = config.getLong("MAX_FILE_SIZE");
		HTTP_VALIDATION = config.getBoolean("HTTP_VALIDATION");
		GENERATE_CHILDREN_REPORTS = config.getBoolean("GENERATE_CHILDREN_REPORTS");
		
		String outDir = config.getString("OUTPUT_DIRECTORY");
		String cacheDir = config.getString("CACHE_DIRECTORY");
		
		if(outDir != null)
			OUTPUT_DIRECTORY = Files.createDirectories(Paths.get(outDir));
		if(cacheDir != null)
			CACHE_DIRECTORY = Files.createDirectories(Paths.get(cacheDir));
	}

	public static void print() {
		_logger.debug("MAX_FILE_SIZE: {}", MAX_FILE_SIZE);
		_logger.debug("HTTP_VALIDATION: {}", HTTP_VALIDATION);
		_logger.debug("GENERATE_CHILDREN_REPORTS: {}", GENERATE_CHILDREN_REPORTS);
		_logger.debug("OUTPUT_DIRECTORY: {}", OUTPUT_DIRECTORY);
		_logger.debug("CACHE_DIRECTORY: {}", CACHE_DIRECTORY);
	}
	
	public static void main(String[] args) throws Exception{
		initDefault();
		print();
	}

}

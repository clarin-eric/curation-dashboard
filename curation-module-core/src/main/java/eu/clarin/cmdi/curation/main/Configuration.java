package eu.clarin.cmdi.curation.main;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

	private static Logger _logger = LoggerFactory.getLogger(Configuration.class);

	public static String SCORE_NUMERIC_DISPLAY_FORMAT;
	public static String TIMESTAMP_DISPLAY_FORMAT;
	public static long MAX_FILE_SIZE;
	public static boolean HTTP_VALIDATION;
	public static boolean SAVE_REPORT;
	public static Path OUTPUT_DIRECTORY = null;
	public static Path CACHE_DIRECTORY = null;
	public static Collection<String> FACETS = null;

	public static boolean COLLECTION_MODE = false; //when true values wont be extracted for facet "text", saves a lot of time in collection assessment

	public static void init(String file) throws IOException {
		_logger.info("Initializing configuration from {}", file);
		Properties config = new Properties();
		config.load(new FileInputStream(file));		 
		readProperties(config);
		
		//readProperties(new PropertiesConfiguration(file));
	}
	
	public static void initDefault() throws IOException {
		_logger.info("Initializing configuration with default config file");
		Properties config = new Properties();
		config.load(Configuration.class.getResourceAsStream("/config.properties"));
		readProperties(config);
		//readProperties(new PropertiesConfiguration("config.properties"));
	}
	
	private static void readProperties(Properties config) throws IOException{
		
		SCORE_NUMERIC_DISPLAY_FORMAT = config.getProperty("SCORE_NUMERIC_DISPLAY_FORMAT");
		TIMESTAMP_DISPLAY_FORMAT = config.getProperty("TIMESTAMP_DISPLAY_FORMAT");
		MAX_FILE_SIZE = Long.parseLong(config.getProperty("MAX_FILE_SIZE"));
		HTTP_VALIDATION = Boolean.parseBoolean(config.getProperty("HTTP_VALIDATION"));
		SAVE_REPORT = Boolean.parseBoolean(config.getProperty("SAVE_REPORT"));
		
		String[] facets = config.getProperty("FACETS").split(",");
		FACETS = Arrays.asList(facets).stream().map(f -> f.trim()).collect(Collectors.toList());	
		
		String outDir = config.getProperty("OUTPUT_DIRECTORY");
		String cacheDir = config.getProperty("CACHE_DIRECTORY");
		
		if(outDir != null && !outDir.isEmpty())
			OUTPUT_DIRECTORY = Files.createDirectories(Paths.get(outDir));
		if(cacheDir != null && !cacheDir.isEmpty())
			CACHE_DIRECTORY = Files.createDirectories(Paths.get(cacheDir));
		
		
		//HTTP Link checker java 1.7+ issue workaround
		//consider to move it to he command line: java -Djsse.enableSNIExtension=false yourClass
		System.setProperty("jsse.enableSNIExtension", "false");
		
	}
}

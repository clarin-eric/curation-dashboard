package eu.clarin.cmdi.curation.main;

import java.io.File;
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

import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;

public class Configuration {

    private static Logger _logger = LoggerFactory.getLogger(Configuration.class);

    public static String SCORE_NUMERIC_DISPLAY_FORMAT;
    public static String TIMESTAMP_DISPLAY_FORMAT;
    public static long MAX_FILE_SIZE;
    public static boolean HTTP_VALIDATION;
    public static boolean SAVE_REPORT;
    public static Path OUTPUT_DIRECTORY = null;
    public static Path CACHE_DIRECTORY = null;
    public static Path COLLECTION_HTML_DIRECTORY = null;
    public static Collection<String> FACETS = null;
    public static int REDIRECT_FOLLOW_LIMIT;
    public static int TIMEOUT;
    private static final int TIMEOUTDEFAULT = 5000;//in ms(if config file doesnt have it)
    
    public static VloConfig VLO_CONFIG;
    public static boolean DATABASE;
    public static String DATABASE_NAME;
    public static String DATABASE_URI;
    public static String USERAGENT;

    //this is a boolean that is set by core-module(false) and web-module(true)
    public static boolean enableProfileLoadTimer = false;

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

    private static void readProperties(Properties config) throws IOException {

        SCORE_NUMERIC_DISPLAY_FORMAT = config.getProperty("SCORE_NUMERIC_DISPLAY_FORMAT");
        TIMESTAMP_DISPLAY_FORMAT = config.getProperty("TIMESTAMP_DISPLAY_FORMAT");
        MAX_FILE_SIZE = Long.parseLong(config.getProperty("MAX_FILE_SIZE"));
        HTTP_VALIDATION = Boolean.parseBoolean(config.getProperty("HTTP_VALIDATION"));
        SAVE_REPORT = Boolean.parseBoolean(config.getProperty("SAVE_REPORT"));

        String timeout = config.getProperty("TIMEOUT");
        if (timeout == null || timeout.isEmpty()) {
            _logger.info("Timeout is not specified in config.properties file. Default timeout is assumed: " + TIMEOUTDEFAULT + "ms.");
            TIMEOUT = TIMEOUTDEFAULT;
        } else {
            TIMEOUT = Integer.parseInt(timeout);
        }


        String[] facets = config.getProperty("FACETS").split(",");
        FACETS = Arrays.asList(facets).stream().map(f -> f.trim()).collect(Collectors.toList());

        String outDir = config.getProperty("OUTPUT_DIRECTORY");
        String cacheDir = config.getProperty("CACHE_DIRECTORY");
        String htmlDir = config.getProperty("COLLECTION_HTML_DIRECTORY");

        if (outDir != null && !outDir.isEmpty()) {
            OUTPUT_DIRECTORY = Files.createDirectories(Paths.get(outDir));
        }
        if (cacheDir != null && !cacheDir.isEmpty()) {
            CACHE_DIRECTORY = Files.createDirectories(Paths.get(cacheDir));
        }

        if(htmlDir !=null && !htmlDir.isEmpty()){
            COLLECTION_HTML_DIRECTORY = Files.createDirectories(Paths.get(htmlDir));
        }

        String redirectFollowLimit = config.getProperty("REDIRECT_FOLLOW_LIMIT");
        if (redirectFollowLimit != null && !redirectFollowLimit.isEmpty()) {
            REDIRECT_FOLLOW_LIMIT = Integer.parseInt(redirectFollowLimit);
        }

        DATABASE = Boolean.parseBoolean(config.getProperty("DATABASE"));
        if (DATABASE) {
            DATABASE_NAME = config.getProperty("DATABASE_NAME");
            DATABASE_URI = config.getProperty("DATABASE_URI");
        }
        
        String vloConfigLocation = config.getProperty("VLO_CONFIG_LOCATION");
        
        if(vloConfigLocation == null || vloConfigLocation.isEmpty()) {
            _logger.warn("loading default VloConfig.xml from vlo-commons.jar - PROGRAM BUT WILL PROBALY DELIVER UNATTENDED RESULTS!!!");
            _logger.warn("make sure to define a valid VLO_CONFIG_LOCATION in the file config.properties");
            VLO_CONFIG = new DefaultVloConfigFactory().newConfig();
        }
        else {
            _logger.info("loading VloConfig.xml from location {}", vloConfigLocation);
            VLO_CONFIG = new XmlVloConfigFactory(new File(config.getProperty("VLO_CONFIG_LOCATION")).toURI().toURL()).newConfig();
        }

        USERAGENT = config.getProperty("USERAGENT");

    }
}

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

    private static Logger logger = LoggerFactory.getLogger(Configuration.class);

    public static String SCORE_NUMERIC_DISPLAY_FORMAT;
    public static String TIMESTAMP_DISPLAY_FORMAT;
    public static long MAX_FILE_SIZE;
    public static boolean HTTP_VALIDATION;
    public static boolean SAVE_REPORT;
    public static Path OUTPUT_DIRECTORY = null;
    public static Path CACHE_DIRECTORY = null;
    public static Collection<String> FACETS = null;
    public static int REDIRECT_FOLLOW_LIMIT;
    public static int TIMEOUT;
    private static final int TIMEOUTDEFAULT = 5000;//in ms(if config file doesnt have it)
    public static boolean DATABASE;

    //this is a boolean that is set by core-module(false) and web-module(true)
    public static boolean enableProfileLoadTimer = false;

    public static boolean COLLECTION_MODE = false; //when true values wont be extracted for facet "text", saves a lot of time in collection assessment

    public static void init(String file) throws IOException {
        logger.info("Initializing configuration from {}", file);
        Properties config = new Properties();
        config.load(new FileInputStream(file));
        readProperties(config);
        //readProperties(new PropertiesConfiguration(file));
    }

    public static void initDefault() throws IOException {
        logger.info("Initializing configuration with default config file");
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
            logger.info("Timeout is not specified in config.properties file. Default timeout is assumed: "+TIMEOUTDEFAULT+"ms.");
            TIMEOUT = TIMEOUTDEFAULT;
        }else{
            TIMEOUT = Integer.parseInt(timeout);
        }


        String[] facets = config.getProperty("FACETS").split(",");
        FACETS = Arrays.asList(facets).stream().map(f -> f.trim()).collect(Collectors.toList());

        String outDir = config.getProperty("OUTPUT_DIRECTORY");
        String cacheDir = config.getProperty("CACHE_DIRECTORY");

        if (outDir != null && !outDir.isEmpty()) {
            OUTPUT_DIRECTORY = Files.createDirectories(Paths.get(outDir));
        }
        if (cacheDir != null && !cacheDir.isEmpty()) {
            CACHE_DIRECTORY = Files.createDirectories(Paths.get(cacheDir));
        }

        String redirectFollowLimit = config.getProperty("REDIRECT_FOLLOW_LIMIT");
        if (redirectFollowLimit != null && !redirectFollowLimit.isEmpty()) {
            REDIRECT_FOLLOW_LIMIT = Integer.parseInt(redirectFollowLimit);
        }

        DATABASE = Boolean.parseBoolean(config.getProperty("DATABASE"));

    }
}

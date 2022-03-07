package eu.clarin.cmdi.curation.main;

import eu.clarin.cmdi.rasa.helpers.RasaFactory;
import eu.clarin.cmdi.rasa.helpers.impl.RasaFactoryBuilderImpl;
import eu.clarin.cmdi.rasa.linkResources.CheckedLinkResource;
import eu.clarin.cmdi.rasa.linkResources.LinkToBeCheckedResource;
import eu.clarin.cmdi.vlo.config.DefaultVloConfigFactory;
import eu.clarin.cmdi.vlo.config.VloConfig;
import eu.clarin.cmdi.vlo.config.XmlVloConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class Configuration {

   private static Logger logger = LoggerFactory.getLogger(Configuration.class);

   // prevent milliseconds
   public static Timestamp REPORT_GENERATION_DATE = new Timestamp((System.currentTimeMillis() / 1000) * 1000);

   public static String SCORE_NUMERIC_DISPLAY_FORMAT;
   public static String TIMESTAMP_DISPLAY_FORMAT;
   public static long MAX_FILE_SIZE;
   public static boolean SAVE_REPORT;
   public static Path OUTPUT_DIRECTORY = null;
   public static Path CACHE_DIRECTORY = null;
   public static int THREADPOOL_SIZE = 100;
   public static String LINK_DATA_SOURCE;
   public static Collection<String> FACETS = null;
   public static int REDIRECT_FOLLOW_LIMIT;
   public static int TIMEOUT;
   public static String DOC_URL;

   public static VloConfig VLO_CONFIG;

   public static String USERAGENT;
   public static String BASE_URL;

   public static CheckedLinkResource checkedLinkResource;
   public static LinkToBeCheckedResource linkToBeCheckedResource;

   // this is a boolean that is set by core-module(false) and web-module(true)
   public static boolean enableProfileLoadTimer = false;

   public static boolean COLLECTION_MODE = false; // when true values wont be extracted for facet "text", saves a lot of
                                                  // time in collection assessment

   private static RasaFactory factory;

   public static void init(String file) throws IOException {
      logger.info("Initializing configuration from {}", file);
      Properties config = new Properties();
      config.load(new FileInputStream(file));
      readProperties(config);
      // readProperties(new PropertiesConfiguration(file));
   }

   public static void initDefault() throws IOException {
      logger.info("Initializing configuration with default config file");
      Properties config = new Properties();
      config.load(Configuration.class.getResourceAsStream("/config.properties"));
      readProperties(config);
   }

   public static void tearDown() {
      logger.info("Finished report generation. Stopping Curation Module...");
      factory.tearDown();
      logger.info("Curation Module stopped.");
   }

   private static void readProperties(Properties config) throws IOException {

      SCORE_NUMERIC_DISPLAY_FORMAT = config.getProperty("SCORE_NUMERIC_DISPLAY_FORMAT");
      TIMESTAMP_DISPLAY_FORMAT = config.getProperty("TIMESTAMP_DISPLAY_FORMAT");
      MAX_FILE_SIZE = Long.parseLong(config.getProperty("MAX_FILE_SIZE"));
      SAVE_REPORT = Boolean.parseBoolean(config.getProperty("SAVE_REPORT"));

      String timeout = config.getProperty("TIMEOUT");
      if (timeout == null || timeout.isEmpty()) {
         // in ms(if config file doesnt have it)
         int TIMEOUTDEFAULT = 5000;
         logger.info("Timeout is not specified in config.properties file. Default timeout is assumed: " + TIMEOUTDEFAULT
               + "ms.");
         TIMEOUT = TIMEOUTDEFAULT;
      }
      else {
         TIMEOUT = Integer.parseInt(timeout);
      }
      THREADPOOL_SIZE = Integer.parseInt(config.getProperty("THREADPOOL_SIZE", "100"));

      LINK_DATA_SOURCE = config.getProperty("LINK_DATA_SOURCE");

      String[] facets = config.getProperty("FACETS").split(",");
      FACETS = Arrays.stream(facets).map(String::trim).collect(Collectors.toList());

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

      final Properties hikariProperties = new Properties();

      config.entrySet().stream().filter(es -> es.getKey().toString().startsWith("HIKARI."))
            .forEach(es -> hikariProperties.setProperty(es.getKey().toString().substring(7), es.getValue().toString()));

      factory = new RasaFactoryBuilderImpl().getRasaFactory(hikariProperties);
      checkedLinkResource = factory.getCheckedLinkResource();
      linkToBeCheckedResource = factory.getLinkToBeCheckedResource();

      String vloConfigLocation = config.getProperty("VLO_CONFIG_LOCATION");

      if (vloConfigLocation == null || vloConfigLocation.isEmpty()) {
         logger.warn(
               "loading default VloConfig.xml from vlo-commons.jar - PROGRAM WILL WORK BUT WILL PROBABABLY DELIVER UNATTENDED RESULTS!!!");
         logger.warn("make sure to define a valid VLO_CONFIG_LOCATION in the file config.properties");
         VLO_CONFIG = new DefaultVloConfigFactory().newConfig();
      }
      else {
         logger.info("loading VloConfig.xml from location {}", vloConfigLocation);
         VLO_CONFIG = new XmlVloConfigFactory(new File(config.getProperty("VLO_CONFIG_LOCATION")).toURI().toURL())
               .newConfig();
      }

      USERAGENT = config.getProperty("USERAGENT");
      BASE_URL = config.getProperty("BASE_URL");
      if (!BASE_URL.endsWith("/")) {
         BASE_URL += "/";
      }

      DOC_URL = config.getProperty("DOC_URL", "");
   }
}

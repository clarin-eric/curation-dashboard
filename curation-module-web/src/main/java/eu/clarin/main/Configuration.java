package eu.clarin.main;

import eu.clarin.cmdi.rasa.linkResources.CheckedLinkResource;
import eu.clarin.cmdi.rasa.linkResources.LinkToBeCheckedResource;
import eu.clarin.cmdi.rasa.linkResources.StatisticsResource;
import eu.clarin.helpers.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MINUTES;

public class Configuration {

    private static Logger _logger = LoggerFactory.getLogger(Configuration.class);

    public static String VIEW_RESOURCES_PATH;
    public static String OUTPUT_DIRECTORY;
    public static String BASE_URL;
//    private static String DATABASE_URI;
//    private static String DATABASE_NAME;

    public static CheckedLinkResource checkedLinkResource;
    public static LinkToBeCheckedResource linkToBeCheckedResource;
    public static StatisticsResource statisticsResource;

    public static void init(ServletContext servletContext) throws IOException {

        VIEW_RESOURCES_PATH = servletContext.getRealPath("/WEB-INF/classes/view/");
        _logger.info("resources path: " + VIEW_RESOURCES_PATH);

        String path = servletContext.getInitParameter("config.location");

        //this is necessary for core module methods.
        eu.clarin.cmdi.curation.main.Configuration.init(path);

        checkedLinkResource = eu.clarin.cmdi.curation.main.Configuration.checkedLinkResource;
        linkToBeCheckedResource = eu.clarin.cmdi.curation.main.Configuration.linkToBeCheckedResource;
        statisticsResource = eu.clarin.cmdi.curation.main.Configuration.statisticsResource;

        _logger.info("Initializing configuration from: " + path);
        Properties properties = new Properties();
        properties.load(new FileInputStream(path));
        loadVariables(properties);
        scheduleCleaningTemp();

    }

    private static void scheduleCleaningTemp() {
        //scheduler to delete temporary instance and profile reports every day at 1 am.
        Runnable instanceCleaner = () -> {
            String instancesXmlFolder = OUTPUT_DIRECTORY + "/xml/instances";
            String instancesHtmlFolder = OUTPUT_DIRECTORY + "/html/instances";

            try {
                FileManager.cleanFolders(Arrays.asList(instancesXmlFolder, instancesHtmlFolder), null);
                _logger.info("Cleaned temp instances.");
            } catch (IOException e) {
                _logger.error("Error when cleaning instances: " + e.getMessage());
            }
        };

        Runnable profileCleaner = () -> {
            String profilesXmlFolder = OUTPUT_DIRECTORY + "/xml/profiles";
            String profilesHtmlFolder = OUTPUT_DIRECTORY + "/html/profiles";
            try {
                FileManager.cleanFolders(Arrays.asList(profilesHtmlFolder, profilesXmlFolder), "^\\d{13}_.+$");
                _logger.info("Cleaned temp profiles.");
            } catch (IOException e) {
                _logger.error("Error when cleaning profiles: " + e.getMessage());
            }
        };


        long oneAM = LocalDateTime.now().until(LocalDate.now().plusDays(1).atTime(1, 0), ChronoUnit.MINUTES);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(instanceCleaner, oneAM, TimeUnit.DAYS.toMinutes(1), MINUTES);
        scheduler.scheduleAtFixedRate(profileCleaner, oneAM, TimeUnit.DAYS.toMinutes(1), MINUTES);

    }

    private static void loadVariables(Properties properties) {

        OUTPUT_DIRECTORY = properties.getProperty("OUTPUT_DIRECTORY");

        BASE_URL = properties.getProperty("BASE_URL");

//        DATABASE_URI = properties.getProperty("DATABASE_URI");
//        DATABASE_NAME = properties.getProperty("DATABASE_NAME");

    }
}

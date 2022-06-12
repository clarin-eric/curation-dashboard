package eu.clarin.main;

import eu.clarin.helpers.FileManager;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.MINUTES;

@Slf4j
public class Configuration extends eu.clarin.cmdi.curation.main.Configuration{

    public static String VIEW_RESOURCES_PATH;
    public static ArrayList<String> clarinCollections;
    public static ArrayList<String> europeanaCollections;

    public static void init(ServletContext servletContext) throws IOException {

        VIEW_RESOURCES_PATH = servletContext.getRealPath("/WEB-INF/classes/view/");

        String path = System.getenv("CONFIG_LOCATION");

        //this is necessary for core module methods.
        eu.clarin.cmdi.curation.main.Configuration.init(path);


        log.info("Initializing configuration from: " + path);
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
                log.info("Cleaned temp instances.");
            } catch (IOException e) {
                log.error("Error when cleaning instances: " + e.getMessage());
            }
        };

        Runnable profileCleaner = () -> {
            String profilesXmlFolder = OUTPUT_DIRECTORY + "/xml/profiles";
            String profilesHtmlFolder = OUTPUT_DIRECTORY + "/html/profiles";
            try {
                FileManager.cleanFolders(Arrays.asList(profilesHtmlFolder, profilesXmlFolder), "^\\d{13}_.+$");
                log.info("Cleaned temp profiles.");
            } catch (IOException e) {
                log.error("Error when cleaning profiles: " + e.getMessage());
            }
        };


        long oneAM = LocalDateTime.now().until(LocalDate.now().plusDays(1).atTime(1, 0), ChronoUnit.MINUTES);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(instanceCleaner, oneAM, TimeUnit.DAYS.toMinutes(1), MINUTES);
        scheduler.scheduleAtFixedRate(profileCleaner, oneAM, TimeUnit.DAYS.toMinutes(1), MINUTES);

    }

    private static void loadVariables(Properties properties) {

        //collection categorizing into europeana and clarin based on the data folder

        try (Stream<Path> walk = Files.walk(Paths.get(DATA_DIRECTORY + "/clarin/results/cmdi/"))) {
            clarinCollections = (ArrayList<String>) walk.filter(Files::isDirectory).filter(path -> !path.getFileName().toString().equals("cmdi")).map(file -> file.getFileName().toString()).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Error when reading folders from :" + DATA_DIRECTORY + "/clarin/results/cmdi, Message:" + e.getMessage());
        }

        try (Stream<Path> walk = Files.walk(Paths.get(DATA_DIRECTORY + "/europeana/results/cmdi/"))) {
            europeanaCollections = (ArrayList<String>) walk.filter(Files::isDirectory).filter(path -> !path.getFileName().toString().equals("cmdi")).map(file -> file.getFileName().toString()).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Error when reading folders from :" + DATA_DIRECTORY + "/europeana/results/cmdi, Message:" + e.getMessage());
        }
        log.info("Clarin collections: " + clarinCollections);
        log.info("Europeana collections: " + europeanaCollections);

        //create Instances directory if it doesn't exist
        try {
            Path path = Paths.get(OUTPUT_DIRECTORY + "/xml/instances");
            Files.createDirectories(path);
            path = Paths.get(OUTPUT_DIRECTORY + "/html/instances");
            Files.createDirectories(path);
            path = Paths.get(OUTPUT_DIRECTORY + "/xml/profiles");
            Files.createDirectories(path);
            path = Paths.get(OUTPUT_DIRECTORY + "/html/profiles");
            Files.createDirectories(path);
        } catch (IOException e) {
            log.error("There was an error creating the profiles/instances xml/html folder: " + e.getMessage());
        }
    }
}

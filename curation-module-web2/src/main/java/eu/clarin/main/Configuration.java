package eu.clarin.main;

import eu.clarin.helpers.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

    private static Logger _logger = LoggerFactory.getLogger(Configuration.class);

    public static String VIEW_RESOURCES_PATH;
    public static String OUTPUT_DIRECTORY;
    public static String GENERIC_HTML;
    public static String BASE_URL;

    public static void init(ServletContext servletContext) throws IOException {

        VIEW_RESOURCES_PATH = servletContext.getRealPath("/WEB-INF/classes/view/");
        _logger.info("resources path: " + VIEW_RESOURCES_PATH);

        String path = servletContext.getInitParameter("config.location");

        _logger.info("Initializing configuration from: " + path);
        Properties properties = new Properties();
        properties.load(new FileInputStream(path));
        loadVariables(properties);

    }

    private static void loadVariables(Properties properties) throws IOException {

        OUTPUT_DIRECTORY = properties.getProperty("OUTPUT_DIRECTORY");

        GENERIC_HTML = FileManager.readFile(VIEW_RESOURCES_PATH + "/html/generic.html");

        BASE_URL = properties.getProperty("BASE_URL");

    }
}

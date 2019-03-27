package eu.clarin.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuration {

    private static Logger _logger = LoggerFactory.getLogger(Configuration.class);

    public static String resourcesPath;

    public static void init(ServletContext servletContext) throws IOException {

        resourcesPath = servletContext.getRealPath("/WEB-INF/classes/view/");
        _logger.info("resources path" + resourcesPath);

        String path = servletContext.getInitParameter("config.location");


        _logger.info("Initializing configuration from " + path);
        Properties properties = new Properties();
        properties.load(new FileInputStream(path));
        loadVariables(properties);

    }

    private static void loadVariables(Properties properties) {
        //todo
    }
}

package eu.clarin.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.IOException;

@WebListener
public class ApplicationInit implements ServletContextListener {

    private static final Logger _logger = LoggerFactory.getLogger(ApplicationInit.class);

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {

            ServletContext servletContext = servletContextEvent.getServletContext();
            Configuration.init(servletContext);

            //todo if there are more initializaion

        } catch (IOException e) {
            _logger.error("There was a problem loading the properties file.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        //todo if there is any database connection or so
    }


}

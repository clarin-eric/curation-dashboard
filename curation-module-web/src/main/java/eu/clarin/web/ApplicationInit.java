package eu.clarin.web;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.clarin.cmdi.curation.main.Configuration;

/**
 * @author dostojic
 *
 */
public class ApplicationInit implements ServletContextListener {
	
	static final Logger _logger = LoggerFactory.getLogger(ApplicationInit.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			Configuration.init(Thread.currentThread().getContextClassLoader().getResourceAsStream("curate.properties"));
			Configuration.HTTP_VALIDATION = true;
			
			// init shared
			Shared.init();
			
		} catch (ConfigurationException | IOException e) {
			_logger.error("", e);
			throw new RuntimeException("Unable to initialize configuration with default properties file", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}

}

/**
 * 
 */
package eu.clarin.web;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.configuration.ConfigurationException;

import eu.clarin.cmdi.curation.main.Configuration;

/**
 * @author dostojic
 *
 */
public class ApplicationInit implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		try {
			File configDir = new File(System.getProperty("catalina.base"), "conf");
			File configFile = new File(configDir, "curate.properties");
			Configuration.init(configFile);
			Configuration.HTTP_VALIDATION = true;
		} catch (ConfigurationException | IOException e) {
			throw new RuntimeException("Unable to initialize configuration with default properties file", e);
		}

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}

}

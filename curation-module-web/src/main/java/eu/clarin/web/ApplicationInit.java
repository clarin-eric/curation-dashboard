/**
 * 
 */
package eu.clarin.web;

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
			Configuration.initDefault();
			Configuration.HTTP_VALIDATION = true;
		} catch (ConfigurationException | IOException e) {
			throw new RuntimeException("Unable to initialize configuration with default properties file", e);
		}

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}

}

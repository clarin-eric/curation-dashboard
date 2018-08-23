package eu.clarin.web;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
			ServletContext sc = sce.getServletContext();
			String configLocation = sc.getInitParameter("config.location");
			if(configLocation != null){
				Configuration.init(configLocation);
			}else
				Configuration.initDefault();
			Configuration.HTTP_VALIDATION = true;
			Configuration.enableProfileLoadTimer = true;
			
			// init shared
			Shared.init();
			
		} catch (IOException e) {
			_logger.error("", e);
			throw new RuntimeException("Unable to initialize configuration with default properties file", e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

	}

}

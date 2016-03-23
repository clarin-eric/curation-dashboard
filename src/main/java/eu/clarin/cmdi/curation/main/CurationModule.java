package eu.clarin.cmdi.curation.main;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

import org.apache.commons.configuration.ConfigurationException;

import eu.clarin.cmdi.curation.report.Report;

public class CurationModule implements CurationModuleInterface{
	
	
	public CurationModule(Path configuration){
		try {
			Configuration.init(configuration.toFile());
		} catch (ConfigurationException | IOException e) {
			throw new RuntimeException("Unable to initialize configuration", e);
		}
	}

	@Override
	public Report processCMDProfile(String profileId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Report processCMDProfile(URL url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Report processCMDInstance(Path file) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Report processCMDInstance(URL url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Report processCollection(Path path) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Report aggregateReports(Collection<Report> reports) {
		// TODO Auto-generated method stub
		return null;
	}

}

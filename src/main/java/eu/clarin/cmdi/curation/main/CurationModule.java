package eu.clarin.cmdi.curation.main;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

import org.apache.commons.configuration.ConfigurationException;

import eu.clarin.cmdi.curation.cr.CRService;
import eu.clarin.cmdi.curation.entities.CMDInstance;
import eu.clarin.cmdi.curation.entities.CMDProfile;
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
		return new CMDProfile(profileId).generateReport();
	}

	@Override
	public Report processCMDProfile(URL url) {
		String profileId = url.toString().substring(CRService.REST_API.length());
		if(profileId.endsWith("/"))
			profileId = profileId.substring(0, profileId.length()-1);
		
		return new CMDProfile(profileId).generateReport();
	}

	@Override
	public Report processCMDInstance(Path file) {
		return new CMDInstance(file).generateReport();
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

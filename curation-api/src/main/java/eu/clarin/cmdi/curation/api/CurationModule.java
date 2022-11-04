package eu.clarin.cmdi.curation.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

import eu.clarin.cmdi.curation.api.report.Report;


public interface CurationModule {
	
    public Report processCMDProfile(String profileId);
    
    public Report processCMDProfile(Path path) throws MalformedURLException;
	
	
	public Report processCMDProfile(URL schemaLocation);
	
	/*
	 * throws Exception if file doesn't exist or is invalid
	 */
	public Report processCMDInstance(Path file);
	
	public Report processCMDInstance(URL url);
	
	public Report processCollection(Path path);
	
	public Report aggregateReports(Collection<Report> reports);
}

package eu.clarin.cmdi.curation.main;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

import eu.clarin.cmdi.curation.report.Report;

public interface CurationModuleInterface {
	
	
	public Report processCMDProfile(String profileId);
	
	public Report processCMDProfile(URL url);
	
	/*
	 * throws Exception if file doesn't exist or is invalid
	 */
	public Report processCMDInstance(Path file) throws IOException;
	
	public Report processCMDInstance(URL url) throws IOException;
	
	public Report processCollection(Path path);
	
	public Report aggregateReports(Collection<Report> reports);
}

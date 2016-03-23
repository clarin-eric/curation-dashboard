package eu.clarin.cmdi.curation.main;

import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

import eu.clarin.cmdi.curation.report.Report;

public interface CurationModuleInterface {
	
	
	public Report processCMDProfile(String profileId);
	
	public Report processCMDProfile(URL url);
	
	public Report processCMDInstance(Path file);
	
	public Report processCMDInstance(URL url);
	
	public Report processCollection(Path path);
	
	public Report aggregateReports(Collection<Report> reports);
}

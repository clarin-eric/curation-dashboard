package eu.clarin.cmdi.curation.main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

import javax.xml.transform.TransformerException;

import eu.clarin.cmdi.curation.report.Report;

public interface CurationModuleInterface {
	
    public Report processCMDProfile(String profileId);
    
    public Report processCMDProfile(Path path) throws MalformedURLException, IOException;
	
	
	public Report processCMDProfile(URL schemaLocation) throws IOException;
	
	/*
	 * throws Exception if file doesn't exist or is invalid
	 */
	public Report processCMDInstance(Path file) throws IOException, TransformerException;
	
	public Report processCMDInstance(URL url) throws IOException;
	
	public Report processCollection(Path path) throws IOException;
	
	public Report aggregateReports(Collection<Report> reports);
}

package eu.clarin.cmdi.curation.main;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

import javax.xml.transform.TransformerException;

import eu.clarin.cmdi.curation.report.Report;

public interface CurationModuleInterface {
	
	
	public Report processCMDProfile(String profileId) throws InterruptedException;
	
	public Report processCMDProfile(URL schemaLocation) throws InterruptedException;
	
	/*
	 * throws Exception if file doesn't exist or is invalid
	 */
	public Report processCMDInstance(Path file) throws IOException, InterruptedException, TransformerException;
	
	public Report processCMDInstance(URL url) throws IOException, InterruptedException;
	
	public Report processCollection(Path path) throws IOException, InterruptedException;
	
	public Report aggregateReports(Collection<Report> reports);
}
